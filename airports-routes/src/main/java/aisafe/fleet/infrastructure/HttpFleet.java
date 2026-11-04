package aisafe.fleet.infrastructure;

import aisafe.fleet.application.Fleet;
import aisafe.fleet.domain.AircraftInfo;
import aisafe.fleet.domain.FleetUnavailableException;
import aisafe.shared.infrastructure.peers.PeerHeaders;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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
 * {@link Fleet} over HTTP: asks aircraft-maintenance through the entry (Envoy) of this VM, which
 * sends the request to a ready replica and repeats it on another one when that replica fails.
 * The replica asked finds the aircraft on its peers if it does not hold it.
 *
 * <p>The user's token goes with each request, so aircraft-maintenance applies its own access rules,
 * and so does the correlation id, so one client request can be followed across services.</p>
 */
@Component
@ConditionalOnProperty(name = "aisafe.events.enabled", havingValue = "false", matchIfMissing = true)
public class HttpFleet implements Fleet {

    private static final Logger log = LoggerFactory.getLogger(HttpFleet.class);

    /** Where {@code RequestTracingFilter} keeps the correlation id of the current request. */
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    private final RestClient restClient;

    public HttpFleet(@Value("${aisafe.services.aircraft-maintenance.url}") String baseUrl,
                     @Value("${aisafe.services.connect-timeout-ms:1000}") long connectTimeoutMs,
                     @Value("${aisafe.services.read-timeout-ms:10000}") long readTimeoutMs) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    @Override
    public Optional<AircraftInfo> findAircraft(String registration) {
        return get("/internal/aircrafts/{id}", registration, AircraftInfo.class);
    }

    @Override
    public boolean aircraftModelExists(String modelName) {
        return get("/internal/aircraft-models/{id}", modelName, AircraftModelInfo.class).isPresent();
    }

    private <T> Optional<T> get(String path, String id, Class<T> type) {
        try {
            return Optional.ofNullable(restClient.get()
                    .uri(path, id)
                    .headers(HttpFleet::copyFromCurrentRequest)
                    .retrieve()
                    .body(type));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            log.warn("aircraft-maintenance did not answer GET {} ({}): {}", path, id, e.getMessage());
            throw new FleetUnavailableException("aircraft-maintenance is not available, try again.");
        }
    }

    private static void copyFromCurrentRequest(HttpHeaders headers) {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization != null) {
                headers.set(HttpHeaders.AUTHORIZATION, authorization);
            }
        }
        String correlationId = MDC.get(CORRELATION_ID_MDC_KEY);
        if (correlationId != null) {
            headers.set(PeerHeaders.CORRELATION_ID, correlationId);
        }
    }

    /** Body of contract C3: only whether the model exists matters here. */
    record AircraftModelInfo(String modelName, String manufacturer) {
    }
}
