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
        assertEquals(new Gate("A1"), new Gate("A1"));
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
