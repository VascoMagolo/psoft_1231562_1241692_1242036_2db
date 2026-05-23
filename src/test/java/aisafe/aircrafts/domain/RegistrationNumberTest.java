package aisafe.aircrafts.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationNumberTest {

    @Test
    void ensureValidRegistrationNumberIsCreated() {
        RegistrationNumber reg = new RegistrationNumber("CS-TPA");
        assertEquals("CS-TPA", reg.getNumber());
    }

    @Test
    void ensureLowercaseIsNormalizedToUpperCase() {
        RegistrationNumber reg = new RegistrationNumber("cs-tpa");
        assertEquals("CS-TPA", reg.getNumber());
    }

    @Test
    void ensureNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new RegistrationNumber(null));
    }

    @Test
    void ensureEmptyStringThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new RegistrationNumber(""));
    }

    @Test
    void ensureTooShortSuffixThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new RegistrationNumber("CS-TP"));
    }

    @Test
    void ensureMissingHyphenThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new RegistrationNumber("CSTPA"));
    }

    @Test
    void ensureDigitInPrefixThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new RegistrationNumber("1S-TPA"));
    }
}
