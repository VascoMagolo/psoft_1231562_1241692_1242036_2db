package aisafe.fleet.infrastructure.readmodel;

import aisafe.fleet.domain.AircraftInfo;
import aisafe.fleet.domain.FleetUnavailableException;
import aisafe.shared.infrastructure.messaging.CommandBus;
import aisafe.shared.infrastructure.messaging.CommandUnavailableException;
import aisafe.shared.infrastructure.readmodel.ReadModelStore;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ReadModelFleetTest {

    private final JsonMapper json = JsonMapper.builder().build();
    private final ReadModelStore store = mock(ReadModelStore.class);
    private final CommandBus commands = mock(CommandBus.class);
    private final ReadModelFleet fleet = new ReadModelFleet(store, commands, json);

    private void row(String entity, String key, String view) {
        when(store.find(entity, key)).thenReturn(Optional.of(new ReadModelStore.Row(key, json.readTree(view), 1L)));
    }

    @Test
    void ensureAircraftComesFromTheCopyWithTheFuelConsumptionOfItsModel() {
        row("aircraft", "CS-TKA", "{\"registrationNumber\":\"CS-TKA\",\"status\":\"AVAILABLE\",\"modelName\":\"Airbus A320neo\","
                + "\"seatCapacity\":180,\"range\":6000.0}");
        row("aircraftmodel", "Airbus A320neo", "{\"modelName\":\"Airbus A320neo\",\"fuelCapacity\":24000.0}");

        assertEquals(new AircraftInfo("CS-TKA", "Airbus A320neo", "AVAILABLE", 180, 6000.0, 4.0),
                fleet.findAircraft("cs-tka").orElseThrow());
        assertTrue(fleet.aircraftModelExists("Airbus A320neo"));
    }

    @Test
    void ensureAircraftWhoseModelHasNotArrivedHasNoFuelConsumption() {
        row("aircraft", "CS-TKA", "{\"registrationNumber\":\"CS-TKA\",\"status\":\"AVAILABLE\",\"modelName\":\"New Model\","
                + "\"seatCapacity\":180,\"range\":6000.0}");
        when(store.find("aircraftmodel", "New Model")).thenReturn(Optional.empty());

        assertNull(fleet.findAircraft("CS-TKA").orElseThrow().fuelConsumptionPerDistanceUnit());
    }

    @Test
    void ensureAircraftNotInTheCopyIsAskedToTheOwner() {
        when(store.find(anyString(), anyString())).thenReturn(Optional.empty());
        when(commands.query("aircraft-maintenance", "aircraft.get", Map.of("key", "CS-NEW"))).thenReturn(Optional.of(json.readTree(
                "{\"registrationNumber\":\"CS-NEW\",\"model\":\"Airbus A320neo\",\"status\":\"AVAILABLE\",\"seatCapacity\":180,"
                        + "\"range\":6000.0,\"fuelConsumptionPerDistanceUnit\":4.0}")));

        assertEquals(new AircraftInfo("CS-NEW", "Airbus A320neo", "AVAILABLE", 180, 6000.0, 4.0),
                fleet.findAircraft("cs-new").orElseThrow());
    }

    @Test
    void ensureUnknownAircraftAndModelAreEmpty() {
        when(store.find(anyString(), anyString())).thenReturn(Optional.empty());
        when(commands.query(anyString(), anyString(), any())).thenReturn(Optional.empty());

        assertTrue(fleet.findAircraft("CS-ZZZ").isEmpty());
        assertFalse(fleet.aircraftModelExists("Nothing"));
    }

    @Test
    void ensureOwnerNotAnsweringIsUnavailableNotNonexistent() {
        when(store.find(anyString(), anyString())).thenReturn(Optional.empty());
        when(commands.query(anyString(), anyString(), any())).thenThrow(new CommandUnavailableException("no answer"));

        assertThrows(FleetUnavailableException.class, () -> fleet.findAircraft("CS-ZZZ"));
    }

    @Test
    void ensureAircraftInTheCopyDoesNotAskTheOwner() {
        row("aircraft", "CS-TKA", "{\"registrationNumber\":\"CS-TKA\",\"status\":\"AVAILABLE\",\"modelName\":\"Airbus A320neo\","
                + "\"seatCapacity\":180,\"range\":6000.0}");

        assertTrue(fleet.findAircraft("CS-TKA").isPresent());
        verifyNoInteractions(commands);
    }
}
