package aisafe.fleet.infrastructure.messaging;

import aisafe.fleet.application.Fleet;
import aisafe.fleet.domain.AircraftConflictException;
import aisafe.fleet.domain.AircraftInfo;
import aisafe.fleet.domain.AircraftNotFoundException;
import aisafe.fleet.domain.FleetUnavailableException;
import aisafe.fleet.domain.FlightCopy;
import aisafe.fleet.domain.RouteConflictException;
import aisafe.fleet.domain.RouteInfo;
import aisafe.fleet.domain.RouteNotFoundException;
import aisafe.shared.infrastructure.messaging.CommandBus;
import aisafe.shared.infrastructure.messaging.CommandReply;
import aisafe.shared.infrastructure.messaging.CommandUnavailableException;
import aisafe.shared.infrastructure.readmodel.ReadModelStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Optional;

/**
 * {@link Fleet} over RabbitMQ, when events are on (Assignment 2). Without events, {@code HttpFleet}
 * does all of this over HTTP.
 *
 * <ul>
 *   <li><b>Queries</b> (C1, C2, C3): read from this replica's copy of the aircraft, models and
 *   routes, kept by the events of their owners ({@link FleetProjections}). Local, so scheduling
 *   usually does not wait for the other services, and the answer may be a few moments behind. What
 *   the copy does not have (yet) is asked to the owner over the broker, so an aircraft created a
 *   moment ago is found at once.</li>
 *   <li><b>Commands</b> (C4, C5, steps of the Saga): sent with {@link CommandBus} to the owner,
 *   which checks them against its own data at that moment and answers. A copy read a moment too
 *   early can never reserve an aircraft that is no longer available: the owner refuses it (409)
 *   and the Saga compensates, as before. The answers become the same exceptions as over HTTP.</li>
 * </ul>
 */
@Component
@ConditionalOnProperty(name = "aisafe.events.enabled", havingValue = "true")
public class MessagingFleet implements Fleet {

    private static final Logger log = LoggerFactory.getLogger(MessagingFleet.class);
    static final String RECORD_FLIGHT_COPY = "flightcopy.record";

    private final ReadModelStore store;
    private final CommandBus commands;
    private final ObjectMapper objectMapper;

    public MessagingFleet(ReadModelStore store, CommandBus commands, ObjectMapper objectMapper) {
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
                    // As aircraft-maintenance calculates it: the model's fuel capacity over the range
                    Double fuelCapacity = store.find(FleetProjections.AIRCRAFT_MODEL, model)
                            .map(m -> number(m.view(), "fuelCapacity")).orElse(null);
                    return new AircraftInfo(aircraft.get("registrationNumber").asString(), model,
                            aircraft.get("status").asString(),
                            aircraft.hasNonNull("seatCapacity") ? aircraft.get("seatCapacity").asInt() : null,
                            range, fuelCapacity == null || range == null || range == 0 ? null : fuelCapacity / range);
                });
        return local.isPresent() ? local
                : ask(FleetProjections.AIRCRAFT_MAINTENANCE, FleetProjections.AIRCRAFT + ".get", Map.of("key", key))
                        .map(body -> objectMapper.treeToValue(body, AircraftInfo.class));
    }

    @Override
    public boolean aircraftModelExists(String modelName) {
        return store.find(FleetProjections.AIRCRAFT_MODEL, modelName).isPresent()
                || ask(FleetProjections.AIRCRAFT_MAINTENANCE, FleetProjections.AIRCRAFT_MODEL + ".get",
                Map.of("key", modelName)).isPresent();
    }

    @Override
    public Optional<RouteInfo> findRoute(String origin, String destination) {
        String o = origin.trim().toUpperCase();
        String d = destination.trim().toUpperCase();
        Optional<RouteInfo> local = store.find(FleetProjections.ROUTE, o + "-" + d)
                .map(row -> {
                    JsonNode route = row.view();
                    return new RouteInfo(route.get("origin").asString(), route.get("destination").asString(),
                            route.hasNonNull("status") ? route.get("status").asString() : null,
                            route.hasNonNull("estimatedFlightTime") ? route.get("estimatedFlightTime").asInt() : null,
                            route.hasNonNull("minimumCapacity") ? route.get("minimumCapacity").asInt() : null,
                            number(route, "minimumRange"), number(route, "distanceKm"));
                });
        return local.isPresent() ? local
                : ask(FleetProjections.AIRPORTS_ROUTES, FleetProjections.ROUTE + ".get", Map.of("origin", o, "destination", d))
                        .map(body -> objectMapper.treeToValue(body, RouteInfo.class));
    }

    /**
     * Not in the copy (yet): asked to the owner over the broker (C1, C2 and C3 as queries), so an
     * aircraft or a route created a moment ago is never taken as nonexistent.
     */
    private Optional<JsonNode> ask(String service, String query, Map<String, String> payload) {
        try {
            return commands.query(service, query, payload);
        } catch (CommandUnavailableException e) {
            throw new FleetUnavailableException(service + " is not available, try again.");
        }
    }

    @Override
    public void sendFlightToAircraft(FlightCopy copy) {
        CommandReply reply = send(FleetProjections.AIRCRAFT_MAINTENANCE, copy);
        switch (reply.status()) {
            case 200 -> { }
            case 404 -> throw new AircraftNotFoundException(copy.aircraftRegistration());
            case 409 -> throw new AircraftConflictException("Aircraft " + copy.aircraftRegistration() + " cannot take a flight.");
            default -> throw unavailable(FleetProjections.AIRCRAFT_MAINTENANCE, copy, reply);
        }
    }

    @Override
    public void sendFlightToRoutes(FlightCopy copy) {
        CommandReply reply = send(FleetProjections.AIRPORTS_ROUTES, copy);
        switch (reply.status()) {
            case 200 -> { }
            case 404 -> throw new RouteNotFoundException(copy.originIataCode(), copy.destinationIataCode());
            case 409 -> throw new RouteConflictException("Route " + copy.originIataCode() + "-"
                    + copy.destinationIataCode() + " is inactive or unavailable.");
            default -> throw unavailable(FleetProjections.AIRPORTS_ROUTES, copy, reply);
        }
    }

    private CommandReply send(String service, FlightCopy copy) {
        try {
            // The copy is idempotent (it carries the flight's version): safe to send once more
            return commands.sendIdempotent(service, RECORD_FLIGHT_COPY, copy);
        } catch (CommandUnavailableException e) {
            log.warn("{} did not answer {} {}: {}", service, RECORD_FLIGHT_COPY, copy.id(), e.getMessage());
            throw new FleetUnavailableException(service + " is not available, try again.");
        }
    }

    private static FleetUnavailableException unavailable(String service, FlightCopy copy, CommandReply reply) {
        log.warn("{} answered {} to {} {}: {}", service, reply.status(), RECORD_FLIGHT_COPY, copy.id(), reply.error());
        return new FleetUnavailableException(service + " is not available, try again.");
    }

    private static Double number(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asDouble() : null;
    }
}
