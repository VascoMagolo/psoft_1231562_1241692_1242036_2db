package aisafe.fleet.infrastructure.readmodel;

import aisafe.shared.infrastructure.readmodel.ExternalProjection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The entities of aircraft-maintenance this service keeps in its read model (from the events
 * {@code aircraft.*} and {@code aircraftmodel.*}), instead of asking C1 and C3 on every request.
 */
@Configuration
public class FleetProjections {

    static final String SOURCE = "aircraft-maintenance";
    static final String AIRCRAFT = "aircraft";
    static final String AIRCRAFT_MODEL = "aircraftmodel";

    @Bean
    ExternalProjection aircraftProjection() {
        return external(AIRCRAFT);
    }

    @Bean
    ExternalProjection aircraftModelProjection() {
        return external(AIRCRAFT_MODEL);
    }

    private static ExternalProjection external(String entity) {
        return new ExternalProjection() {
            @Override public String entity() { return entity; }
            @Override public String source() { return SOURCE; }
        };
    }
}
