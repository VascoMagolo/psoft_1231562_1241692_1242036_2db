package aisafe.airports.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TerminalTest {

    @Test
    void ensureValidTerminalIsCreated() {
        Terminal terminal = new Terminal("Terminal 1");
        assertEquals("Terminal 1", terminal.getName());
    }

    @Test
    void ensureNullNameThrowsException() {
        assertThrows(InvalidTerminalException.class, () -> new Terminal(null));
    }

    @Test
    void ensureEmptyNameThrowsException() {
        assertThrows(InvalidTerminalException.class, () -> new Terminal("  "));
    }

    @Test
    void ensureEqualTerminalsAreEqual() {
        assertEquals(new Terminal("Terminal 1"), new Terminal("Terminal 1"));
    }

    @Test
    void ensureDifferentTerminalsAreNotEqual() {
        assertNotEquals(new Terminal("Terminal 1"), new Terminal("Terminal 2"));
    }

    @Test
    void ensureHashCodeIsConsistentWithEquals() {
        assertEquals(new Terminal("Terminal 1").hashCode(), new Terminal("Terminal 1").hashCode());
    }

    @Test
    void ensureToStringContainsName() {
        assertTrue(new Terminal("Terminal 1").toString().contains("Terminal 1"));
    }

    @Test
    void ensureEqualsSameReference() {
        Terminal terminal = new Terminal("Terminal 1");
        assertEquals(terminal, terminal);
    }

    @Test
    void ensureEqualsNull() {
        Terminal terminal = new Terminal("Terminal 1");
        assertFalse(terminal.equals(null));
    }

    @Test
    void ensureEqualsDifferentClass() {
        Terminal terminal = new Terminal("Terminal 1");
        assertFalse(terminal.equals("Terminal 1"));
    }
}
