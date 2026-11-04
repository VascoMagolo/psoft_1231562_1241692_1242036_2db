package aisafe.fleet.infrastructure.messaging;

import aisafe.shared.infrastructure.readmodel.ExternalProjection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The entities of other services this service keeps in its read model, from their events, instead
 * of asking for them on every scheduling: aircraft and aircraft models (aircraft-maintenance,
 * instead of C1 and C3) and routes (airports-routes, instead of C2).
 */
@Configuration
public class FleetProjections {

    static final String AIRCRAFT_MAINTENANCE = "aircraft-maintenance";
    static final String AIRPORTS_ROUTES = "airports-routes";
    static final String AIRCRAFT = "aircraft";
    static final String AIRCRAFT_MODEL = "aircraftmodel";
    static final String ROUTE = "route";

    @Bean
    ExternalProjection aircraftProjection() {
        return external(AIRCRAFT, AIRCRAFT_MAINTENANCE);
    }

    @Bean
    ExternalProjection aircraftModelProjection() {
        return external(AIRCRAFT_MODEL, AIRCRAFT_MAINTENANCE);
    }

    @Bean
    ExternalProjection routeProjection() {
        return external(ROUTE, AIRPORTS_ROUTES);
    }

    private static ExternalProjection external(String entity, String source) {
        return new ExternalProjection() {
            @Override public String entity() { return entity; }
            @Override public String source() { return source; }
        };
    }
}
