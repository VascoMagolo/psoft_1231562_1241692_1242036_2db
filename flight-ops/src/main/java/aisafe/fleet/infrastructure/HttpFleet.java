package aisafe.fleet.infrastructure;

import aisafe.fleet.application.Fleet;
import aisafe.fleet.domain.AircraftConflictException;
import aisafe.fleet.domain.AircraftInfo;
import aisafe.fleet.domain.AircraftNotFoundException;
import aisafe.fleet.domain.FleetUnavailableException;
import aisafe.fleet.domain.FlightCopy;
import aisafe.fleet.domain.RouteConflictException;
import aisafe.fleet.domain.RouteInfo;
import aisafe.fleet.domain.RouteNotFoundException;
import aisafe.shared.infrastructure.peers.PeerHeaders;
import aisafe.shared.infrastructure.security.ServiceTokens;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Optional;

/**
 * {@link Fleet} over HTTP: communicates with aircraft-maintenance and airports-routes.
 * Forwards the user's token (or, outside a user's request, this replica's service token) and the
 * correlation ID, and maps HTTP error status codes to domain exceptions.
 */
@Component
@ConditionalOnProperty(name = "aisafe.events.enabled", havingValue = "false", matchIfMissing = true)
public class HttpFleet implements Fleet {

    private static final Logger log = LoggerFactory.getLogger(HttpFleet.class);

    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    private final RestClient aircraftClient;
    private final RestClient routesClient;
    private final ServiceTokens serviceTokens;

    @Autowired
    public HttpFleet(@Value("${aisafe.services.aircraft-maintenance.url:http://localhost:8081}") String aircraftUrl,
                     @Value("${aisafe.services.airports-routes.url:http://localhost:8082}") String routesUrl,
                     @Value("${aisafe.services.connect-timeout-ms:1000}") long connectTimeoutMs,
                     @Value("${aisafe.services.read-timeout-ms:10000}") long readTimeoutMs,
                     ServiceTokens serviceTokens) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        this.aircraftClient = RestClient.builder().baseUrl(aircraftUrl).requestFactory(requestFactory).build();
        this.routesClient = RestClient.builder().baseUrl(routesUrl).requestFactory(requestFactory).build();
        this.serviceTokens = serviceTokens;
    }

    public HttpFleet(String baseUrl, long connectTimeoutMs, long readTimeoutMs) {
        this(baseUrl, baseUrl, connectTimeoutMs, readTimeoutMs, null);
    }

    @Override
    public Optional<AircraftInfo> findAircraft(String registration) {
        try {
            return Optional.ofNullable(aircraftClient.get()
                    .uri("/internal/aircrafts/{id}", registration)
                    .headers(this::copyFromCurrentRequest)
                    .retrieve()
                    .body(AircraftInfo.class));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            log.warn("aircraft-maintenance did not answer GET /internal/aircrafts/{}: {}", registration, e.getMessage());
            throw new FleetUnavailableException("aircraft-maintenance is not available, try again.");
        }
    }

    @Override
    public boolean aircraftModelExists(String modelName) {
        try {
            return Optional.ofNullable(aircraftClient.get()
                    .uri("/internal/aircraft-models/{id}", modelName)
                    .headers(this::copyFromCurrentRequest)
                    .retrieve()
                    .body(AircraftModelInfo.class)).isPresent();
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (RestClientException e) {
            log.warn("aircraft-maintenance did not answer GET /internal/aircraft-models/{}: {}", modelName, e.getMessage());
            throw new FleetUnavailableException("aircraft-maintenance is not available, try again.");
        }
    }

    @Override
    public Optional<RouteInfo> findRoute(String origin, String destination) {
        try {
            return Optional.ofNullable(routesClient.get()
                    .uri("/internal/routes/{origin}/{destination}", origin, destination)
                    .headers(this::copyFromCurrentRequest)
                    .retrieve()
                    .body(RouteInfo.class));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            log.warn("airports-routes did not answer GET /internal/routes/{}/{}: {}", origin, destination, e.getMessage());
            throw new FleetUnavailableException("airports-routes is not available, try again.");
        }
    }

    @Override
    public void sendFlightToAircraft(FlightCopy copy) {
        try {
            aircraftClient.put()
                    .uri("/internal/flights/{id}", copy.id())
                    .headers(this::copyFromCurrentRequest)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(copy)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound e) {
            throw new AircraftNotFoundException(copy.aircraftRegistration());
        } catch (HttpClientErrorException.Conflict e) {
            throw new AircraftConflictException("Aircraft " + copy.aircraftRegistration() + " cannot take a flight.");
        } catch (RestClientException e) {
            log.warn("aircraft-maintenance did not answer PUT /internal/flights/{}: {}", copy.id(), e.getMessage());
            throw new FleetUnavailableException("aircraft-maintenance is not available, try again.");
        }
    }

    @Override
    public void sendFlightToRoutes(FlightCopy copy) {
        try {
            routesClient.put()
                    .uri("/internal/flights/{id}", copy.id())
                    .headers(this::copyFromCurrentRequest)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(copy)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound e) {
            throw new RouteNotFoundException(copy.originIataCode(), copy.destinationIataCode());
        } catch (HttpClientErrorException.Conflict e) {
            throw new RouteConflictException("Route " + copy.originIataCode() + "-" + copy.destinationIataCode() + " is inactive or unavailable.");
        } catch (RestClientException e) {
            log.warn("airports-routes did not answer PUT /internal/flights/{}: {}", copy.id(), e.getMessage());
            throw new FleetUnavailableException("airports-routes is not available, try again.");
        }
    }

    private void copyFromCurrentRequest(HttpHeaders headers) {
        String authorization = null;
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        }
        // Outside a user's request (sample data at startup, compensations retried later): this
        // replica's own token, role SERVICE
        if (authorization == null && serviceTokens != null) {
            authorization = "Bearer " + serviceTokens.current();
        }
        if (authorization != null) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }
        String correlationId = MDC.get(CORRELATION_ID_MDC_KEY);
        if (correlationId != null) {
            headers.set(PeerHeaders.CORRELATION_ID, correlationId);
        }
    }

    record AircraftModelInfo(String modelName, String manufacturer) {}
}
