package aisafe.fleet.infrastructure.messaging;

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
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MessagingFleetTest {

    private final JsonMapper json = JsonMapper.builder().build();
    private final ReadModelStore store = mock(ReadModelStore.class);
    private final CommandBus commands = mock(CommandBus.class);
    private final MessagingFleet fleet = new MessagingFleet(store, commands, json);
    private final OffsetDateTime departure = OffsetDateTime.of(2027, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC);
    private final FlightCopy copy = new FlightCopy("F-1", departure, departure.plusHours(2), "SCHEDULED", "OPO", "LIS", "CS-TKA", 1);

    private void row(String entity, String key, String view) {
        when(store.find(entity, key)).thenReturn(Optional.of(new ReadModelStore.Row(key, json.readTree(view), 1L)));
    }

    @Test
    void ensureAircraftComesFromTheCopyWithTheFuelConsumptionOfItsModel() {
        row("aircraft", "CS-TKA", "{\"registrationNumber\":\"CS-TKA\",\"status\":\"AVAILABLE\",\"modelName\":\"Airbus A320neo\","
                + "\"seatCapacity\":180,\"range\":6000.0}");
        row("aircraftmodel", "Airbus A320neo", "{\"modelName\":\"Airbus A320neo\",\"fuelCapacity\":24000.0}");

        AircraftInfo aircraft = fleet.findAircraft("cs-tka").orElseThrow();

        assertEquals(new AircraftInfo("CS-TKA", "Airbus A320neo", "AVAILABLE", 180, 6000.0, 4.0), aircraft);
        verifyNoInteractions(commands);
    }

    @Test
    void ensureUnknownAircraftModelAndRouteAreEmptyAfterAskingTheOwner() {
        when(store.find(anyString(), anyString())).thenReturn(Optional.empty());
        when(commands.query(anyString(), anyString(), any())).thenReturn(Optional.empty());

        assertTrue(fleet.findAircraft("CS-ZZZ").isEmpty());
        assertFalse(fleet.aircraftModelExists("Nothing"));
        assertTrue(fleet.findRoute("ZZZ", "YYY").isEmpty());
        verify(commands).query("aircraft-maintenance", "aircraft.get", Map.of("key", "CS-ZZZ"));
        verify(commands).query("airports-routes", "route.get", Map.of("origin", "ZZZ", "destination", "YYY"));
    }

    @Test
    void ensureAircraftCreatedAMomentAgoIsFoundAtTheOwner() {
        when(store.find(anyString(), anyString())).thenReturn(Optional.empty());
        when(commands.query("aircraft-maintenance", "aircraft.get", Map.of("key", "FL-NEW"))).thenReturn(Optional.of(json.readTree(
                "{\"registrationNumber\":\"FL-NEW\",\"model\":\"Airbus A320neo\",\"status\":\"AVAILABLE\",\"seatCapacity\":150,"
                        + "\"range\":5000.0,\"fuelConsumptionPerDistanceUnit\":5.0}")));

        assertEquals(new AircraftInfo("FL-NEW", "Airbus A320neo", "AVAILABLE", 150, 5000.0, 5.0),
                fleet.findAircraft("FL-NEW").orElseThrow());
    }

    @Test
    void ensureRouteNotInTheCopyIsAskedToAirportsRoutes() {
        when(store.find(anyString(), anyString())).thenReturn(Optional.empty());
        when(commands.query("airports-routes", "route.get", Map.of("origin", "LIS", "destination", "OPO"))).thenReturn(Optional.of(json.readTree(
                "{\"originIataCode\":\"LIS\",\"destinationIataCode\":\"OPO\",\"status\":\"ACTIVE\",\"estimatedFlightTime\":55,"
                        + "\"minimumCapacity\":100,\"minimumRange\":300.0,\"distanceKm\":274.03}")));

        assertEquals(new RouteInfo("LIS", "OPO", "ACTIVE", 55, 100, 300.0, 274.03), fleet.findRoute("lis", "opo").orElseThrow());
    }

    @Test
    void ensureOwnerNotAnsweringAQueryIsUnavailable() {
        when(store.find(anyString(), anyString())).thenReturn(Optional.empty());
        when(commands.query(anyString(), anyString(), any())).thenThrow(new CommandUnavailableException("no answer"));

        assertThrows(FleetUnavailableException.class, () -> fleet.findRoute("LIS", "OPO"));
    }

    @Test
    void ensureRouteComesFromTheCopyWithItsDistance() {
        row("route", "OPO-LIS", "{\"origin\":\"OPO\",\"destination\":\"LIS\",\"estimatedFlightTime\":55,\"minimumRange\":300.0,"
                + "\"minimumCapacity\":100,\"status\":\"ACTIVE\",\"distanceKm\":274.03}");

        assertEquals(new RouteInfo("OPO", "LIS", "ACTIVE", 55, 100, 300.0, 274.03), fleet.findRoute("opo", "lis").orElseThrow());
    }

    @Test
    void ensureC4AnswersBecomeTheExceptionsTheSagaHandles() {
        when(commands.sendIdempotent(eq("aircraft-maintenance"), eq("flightcopy.record"), any()))
                .thenReturn(new CommandReply(200, null, null))
                .thenReturn(new CommandReply(404, null, "no aircraft"))
                .thenReturn(new CommandReply(409, null, "in maintenance"))
                .thenReturn(new CommandReply(500, null, "bug"))
                .thenThrow(new CommandUnavailableException("no answer"));

        assertDoesNotThrow(() -> fleet.sendFlightToAircraft(copy));
        assertThrows(AircraftNotFoundException.class, () -> fleet.sendFlightToAircraft(copy));
        assertThrows(AircraftConflictException.class, () -> fleet.sendFlightToAircraft(copy));
        assertThrows(FleetUnavailableException.class, () -> fleet.sendFlightToAircraft(copy));
        assertThrows(FleetUnavailableException.class, () -> fleet.sendFlightToAircraft(copy));
        verify(commands, times(5)).sendIdempotent("aircraft-maintenance", "flightcopy.record", copy);
    }

    @Test
    void ensureC5AnswersBecomeTheExceptionsTheSagaHandles() {
        when(commands.sendIdempotent(eq("airports-routes"), eq("flightcopy.record"), any()))
                .thenReturn(new CommandReply(404, null, "no route"))
                .thenReturn(new CommandReply(409, null, "inactive"))
                .thenThrow(new CommandUnavailableException("no answer"));

        assertThrows(RouteNotFoundException.class, () -> fleet.sendFlightToRoutes(copy));
        assertThrows(RouteConflictException.class, () -> fleet.sendFlightToRoutes(copy));
        assertThrows(FleetUnavailableException.class, () -> fleet.sendFlightToRoutes(copy));
    }
}
