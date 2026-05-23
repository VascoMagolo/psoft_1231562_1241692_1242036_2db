package aisafe.airports.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoordinatesTest {

    @Test
    void ensureValidCoordinatesAreCreated() {
        Coordinates coords = new Coordinates(38.77, -9.13);
        assertEquals(38.77, coords.getLatitude());
        assertEquals(-9.13, coords.getLongitude());
    }

    @Test
    void ensureEdgeLatitudeValuesAreAccepted() {
        assertDoesNotThrow(() -> new Coordinates(-90.0, 0.0));
        assertDoesNotThrow(() -> new Coordinates(90.0, 0.0));
    }

    @Test
    void ensureEdgeLongitudeValuesAreAccepted() {
        assertDoesNotThrow(() -> new Coordinates(0.0, -180.0));
        assertDoesNotThrow(() -> new Coordinates(0.0, 180.0));
    }

    @Test
    void ensureNullLatitudeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Coordinates(null, -9.13));
    }

    @Test
    void ensureNullLongitudeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Coordinates(38.77, null));
    }

    @Test
    void ensureLatitudeBelowMinThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Coordinates(-91.0, 0.0));
    }

    @Test
    void ensureLatitudeAboveMaxThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Coordinates(91.0, 0.0));
    }

    @Test
    void ensureLongitudeBelowMinThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Coordinates(0.0, -181.0));
    }

    @Test
    void ensureLongitudeAboveMaxThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Coordinates(0.0, 181.0));
    }
}
