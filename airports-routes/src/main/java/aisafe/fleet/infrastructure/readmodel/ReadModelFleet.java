package aisafe.fleet.infrastructure.readmodel;

import aisafe.fleet.application.Fleet;
import aisafe.fleet.domain.AircraftInfo;
import aisafe.fleet.domain.FleetUnavailableException;
import aisafe.shared.infrastructure.messaging.CommandBus;
import aisafe.shared.infrastructure.messaging.CommandUnavailableException;
import aisafe.shared.infrastructure.readmodel.ReadModelStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Optional;

/**
 * {@link Fleet} read from this replica's copy of the fleet, when events are on: the aircraft and
 * aircraft models aircraft-maintenance publishes ({@link FleetProjections}). The answer is local,
 * and does not depend on aircraft-maintenance being up; it
 * may be a few moments behind (eventual consistency). What the copy does not have (yet) is asked
 * to aircraft-maintenance over the broker (C1 and C3 as queries), so a new aircraft is found at
 * once. Without events, {@code HttpFleet} asks C1 and C3 over HTTP instead.
 *
 * <p>The fuel consumption of an aircraft is its model's fuel capacity over its range, as
 * aircraft-maintenance calculates it, so it follows a change of the model at once.</p>
 */
@Component
@ConditionalOnProperty(name = "aisafe.events.enabled", havingValue = "true")
public class ReadModelFleet implements Fleet {

    private final ReadModelStore store;
    private final CommandBus commands;
    private final ObjectMapper objectMapper;

    public ReadModelFleet(ReadModelStore store, CommandBus commands, ObjectMapper objectMapper) {
        this.store = store;
        this.commands = commands;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<AircraftInfo> findAircraft(String registration) {
        String key = registration.trim().toUpperCase();
        Optional<AircraftInfo> local = store.find(FleetProjections.AIRCRAFT, key)
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
        return local.isPresent() ? local
                : ask(FleetProjections.AIRCRAFT + ".get", key).map(body -> objectMapper.treeToValue(body, AircraftInfo.class));
    }

    @Override
    public boolean aircraftModelExists(String modelName) {
        return store.find(FleetProjections.AIRCRAFT_MODEL, modelName).isPresent()
                || ask(FleetProjections.AIRCRAFT_MODEL + ".get", modelName).isPresent();
    }

    /**
     * Not in the copy (yet): asked to aircraft-maintenance over the broker, as C1 and C3, so an
     * aircraft created a moment ago is never taken as nonexistent.
     */
    private Optional<JsonNode> ask(String query, String key) {
        try {
            return commands.query(FleetProjections.SOURCE, query, Map.of("key", key));
        } catch (CommandUnavailableException e) {
            throw new FleetUnavailableException("aircraft-maintenance is not available, try again.");
        }
    }

    private static Double number(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asDouble() : null;
    }
}
