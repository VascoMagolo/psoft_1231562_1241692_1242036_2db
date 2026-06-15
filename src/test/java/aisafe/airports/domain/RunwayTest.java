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
        assertThrows(InvalidRunwayException.class, () -> new Runway(null, 3000, "030/210"));
    }

    @Test
    void ensureEmptyNameThrowsException() {
        assertThrows(InvalidRunwayException.class, () -> new Runway("", 3000, "030/210"));
    }

    @Test
    void ensureNullLengthThrowsException() {
        assertThrows(InvalidRunwayException.class, () -> new Runway("03/21", null, "030/210"));
    }

    @Test
    void ensureNullOrientationThrowsException() {
        assertThrows(InvalidRunwayException.class, () -> new Runway("03/21", 3000, null));
    }

    @Test
    void ensureEmptyOrientationThrowsException() {
        assertThrows(InvalidRunwayException.class, () -> new Runway("03/21", 3000, ""));
    }

    @Test
    void ensureEqualRunwaysAreEqual() {
        assertEquals(new Runway("03/21", 3000, "030/210"), new Runway("03/21", 3000, "030/210"));
    }

    @Test
    void ensureDifferentRunwaysAreNotEqual() {
        assertNotEquals(new Runway("03/21", 3000, "030/210"), new Runway("09/27", 2500, "090/270"));
    }

    @Test
    void ensureHashCodeIsConsistentWithEquals() {
        assertEquals(new Runway("03/21", 3000, "030/210").hashCode(), new Runway("03/21", 3000, "030/210").hashCode());
    }

    @Test
    void ensureToStringContainsValues() {
        String str = new Runway("03/21", 3000, "030/210").toString();
        assertTrue(str.contains("03/21"));
        assertTrue(str.contains("3000"));
        assertTrue(str.contains("030/210"));
    }
}
