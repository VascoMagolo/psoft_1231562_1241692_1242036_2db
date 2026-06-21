package aisafe.airports.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GateTest {

    @Test
    void ensureValidGateIsCreated() {
        Gate gate = new Gate("A1");
        assertEquals("A1", gate.getIdentifier());
    }

    @Test
    void ensureNullIdentifierThrowsException() {
        assertThrows(InvalidGateException.class, () -> new Gate(null));
    }

    @Test
    void ensureEmptyIdentifierThrowsException() {
        assertThrows(InvalidGateException.class, () -> new Gate("  "));
    }

    @Test
    void ensureEqualGatesAreEqual() {
        Gate g1 = new Gate("A1");
        Gate g2 = new Gate("A1");
        assertEquals(g1, g2);
        assertEquals(g1, g1);
        assertNotEquals(g1, null);
        assertNotEquals(g1, "not a gate");
    }

    @Test
    void ensureDifferentGatesAreNotEqual() {
        assertNotEquals(new Gate("A1"), new Gate("B2"));
    }

    @Test
    void ensureHashCodeIsConsistentWithEquals() {
        assertEquals(new Gate("A1").hashCode(), new Gate("A1").hashCode());
    }

    @Test
    void ensureToStringContainsIdentifier() {
        assertTrue(new Gate("A1").toString().contains("A1"));
    }
}
