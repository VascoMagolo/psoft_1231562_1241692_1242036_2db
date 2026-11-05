package aisafe.aircrafts.infrastructure;

import aisafe.aircrafts.application.ViewAircraftForServicesUseCase;
import aisafe.aircrafts.application.ViewAircraftModelForServicesUseCase;
import aisafe.aircrafts.application.dtos.AircraftForServicesResponse;
import aisafe.aircrafts.application.dtos.ViewAircraftDetailsRequest;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.shared.application.PeerQueries;
import aisafe.shared.domain.NotFoundException;
import aisafe.shared.infrastructure.messaging.CommandHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriUtils;
import tools.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;

/**
 * Contracts C1 and C3 over RabbitMQ, for the services that keep a copy of the fleet: they read
 * their copy first and ask here only when it does not have the key (e.g. an aircraft created a
 * few milliseconds ago, whose event has not reached them yet). The answer comes from the write
 * model, as over HTTP: this replica's, or a peer's when another replica holds the aircraft.
 */
@Configuration
public class FleetQueryHandlers {

    public static final String AIRCRAFT = "aircraft.get";
    public static final String AIRCRAFT_MODEL = "aircraftmodel.get";

    /** {@code {"key": "CS-TKA"}}: the aircraft as in C1, or 404. */
    @Bean
    CommandHandler aircraftQuery(ViewAircraftForServicesUseCase viewAircraft, PeerQueries peers) {
        return handler(AIRCRAFT, payload -> {
            String registration = payload.get("key").asString().trim().toUpperCase();
            try {
                return viewAircraft.execute(new ViewAircraftDetailsRequest(new RegistrationNumber(registration)));
            } catch (NotFoundException notHere) {
                return peers.fetchFirst("/internal/aircrafts/" + UriUtils.encodePathSegment(registration, StandardCharsets.UTF_8),
                        AircraftForServicesResponse.class).orElseThrow(() -> notHere);
            }
        });
    }

    /** {@code {"key": "Airbus A320neo"}}: the model as in C3, or 404 (models are on every replica). */
    @Bean
    CommandHandler aircraftModelQuery(ViewAircraftModelForServicesUseCase viewModel) {
        return handler(AIRCRAFT_MODEL, payload -> viewModel.execute(payload.get("key").asString()));
    }

    private static CommandHandler handler(String type, java.util.function.Function<JsonNode, Object> body) {
        return new CommandHandler() {
            @Override public String type() { return type; }
            @Override public Object handle(JsonNode payload) { return body.apply(payload); }
        };
    }
}
