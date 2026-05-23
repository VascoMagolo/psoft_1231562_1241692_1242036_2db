package aisafe.airports.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RunwayTest {

    @Test
    void ensureValidRunwayIsCreated() {
        Runway runway = new Runway("03/21", 3000, "030/210");
        assertEquals("03/21", runway.getName());
        assertEquals(3000, runway.getLength());
        assertEquals("030/210", runway.getOrientation());
    }

    @Test
    void ensureNullNameThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Runway(null, 3000, "030/210"));
    }

    @Test
    void ensureEmptyNameThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Runway("", 3000, "030/210"));
    }

    @Test
    void ensureNullLengthThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Runway("03/21", null, "030/210"));
    }

    @Test
    void ensureNullOrientationThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Runway("03/21", 3000, null));
    }

    @Test
    void ensureEmptyOrientationThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Runway("03/21", 3000, ""));
    }
}
