package aisafe.fleet.infrastructure.readmodel;

import aisafe.fleet.application.Fleet;
import aisafe.fleet.domain.AircraftInfo;
import aisafe.shared.infrastructure.readmodel.ReadModelStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.Optional;

/**
 * {@link Fleet} read from this replica's copy of the fleet, when events are on: the aircraft and
 * aircraft models aircraft-maintenance publishes ({@link FleetProjections}). Nothing is asked over
 * the network, so the answer is local and does not depend on aircraft-maintenance being up; it
 * may be a few moments behind (eventual consistency). Without events, {@code HttpFleet} asks C1
 * and C3 instead.
 *
 * <p>The fuel consumption of an aircraft is its model's fuel capacity over its range, as
 * aircraft-maintenance calculates it, so it follows a change of the model at once.</p>
 */
@Component
@ConditionalOnProperty(name = "aisafe.events.enabled", havingValue = "true")
public class ReadModelFleet implements Fleet {

    private final ReadModelStore store;

    public ReadModelFleet(ReadModelStore store) {
        this.store = store;
    }

    @Override
    public Optional<AircraftInfo> findAircraft(String registration) {
        return store.find(FleetProjections.AIRCRAFT, registration.trim().toUpperCase())
                .map(row -> {
                    JsonNode aircraft = row.view();
                    String model = aircraft.get("modelName").asString();
                    Double range = number(aircraft, "range");
                    Double fuelCapacity = store.find(FleetProjections.AIRCRAFT_MODEL, model)
                            .map(m -> number(m.view(), "fuelCapacity")).orElse(null);
                    return new AircraftInfo(aircraft.get("registrationNumber").asString(), model,
                            aircraft.get("status").asString(),
                            aircraft.hasNonNull("seatCapacity") ? aircraft.get("seatCapacity").asInt() : null,
                            range, fuelCapacity == null || range == null || range == 0 ? null : fuelCapacity / range);
                });
    }

    @Override
    public boolean aircraftModelExists(String modelName) {
        return store.find(FleetProjections.AIRCRAFT_MODEL, modelName).isPresent();
    }

    private static Double number(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asDouble() : null;
    }
}
