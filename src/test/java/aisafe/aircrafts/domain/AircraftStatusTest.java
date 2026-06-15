package aisafe.aircrafts.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class AircraftStatusTest {

    @Test
    void ensureValuesAreCorrect() {
        assertEquals(4, AircraftStatus.values().length);
        assertDoesNotThrow(() -> AircraftStatus.valueOf("AVAILABLE"));
        assertDoesNotThrow(() -> AircraftStatus.valueOf("IN_FLIGHT"));
        assertDoesNotThrow(() -> AircraftStatus.valueOf("UNDER_MAINTENANCE"));
        assertDoesNotThrow(() -> AircraftStatus.valueOf("INACTIVE"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"AVAILABLE", "IN_FLIGHT", "UNDER_MAINTENANCE", "INACTIVE", "available", "in_flight"})
    void ensureValidStatus(String status) {
        assertTrue(AircraftStatus.isValid(status));
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVALID", " ", "", "TEST", "MAINTENANCE"})
    void ensureInvalidStatus(String status) {
        assertFalse(AircraftStatus.isValid(status));
    }

    @Test
    void ensureIsValidHandlesNull() {
        assertFalse(AircraftStatus.isValid(null));
    }
}
