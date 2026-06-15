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
        assertThrows(InvalidCoordinatesException.class, () -> new Coordinates(null, -9.13));
    }

    @Test
    void ensureNullLongitudeThrowsException() {
        assertThrows(InvalidCoordinatesException.class, () -> new Coordinates(38.77, null));
    }

    @Test
    void ensureLatitudeBelowMinThrowsException() {
        assertThrows(InvalidCoordinatesException.class, () -> new Coordinates(-91.0, 0.0));
    }

    @Test
    void ensureLatitudeAboveMaxThrowsException() {
        assertThrows(InvalidCoordinatesException.class, () -> new Coordinates(91.0, 0.0));
    }

    @Test
    void ensureLongitudeBelowMinThrowsException() {
        assertThrows(InvalidCoordinatesException.class, () -> new Coordinates(0.0, -181.0));
    }

    @Test
    void ensureLongitudeAboveMaxThrowsException() {
        assertThrows(InvalidCoordinatesException.class, () -> new Coordinates(0.0, 181.0));
    }

    @Test
    void ensureEqualCoordinatesAreEqual() {
        assertEquals(new Coordinates(38.77, -9.13), new Coordinates(38.77, -9.13));
    }

    @Test
    void ensureDifferentCoordinatesAreNotEqual() {
        assertNotEquals(new Coordinates(38.77, -9.13), new Coordinates(51.47, -0.46));
    }

    @Test
    void ensureHashCodeIsConsistentWithEquals() {
        assertEquals(new Coordinates(38.77, -9.13).hashCode(), new Coordinates(38.77, -9.13).hashCode());
    }

    @Test
    void ensureToStringContainsValues() {
        String str = new Coordinates(38.77, -9.13).toString();
        assertTrue(str.contains("38.77"));
        assertTrue(str.contains("-9.13"));
    }
}
