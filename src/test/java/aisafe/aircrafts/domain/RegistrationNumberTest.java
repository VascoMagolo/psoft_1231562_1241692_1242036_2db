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
        assertThrows(InvalidRegistrationNumberException.class, () -> new RegistrationNumber(null));
    }

    @Test
    void ensureEmptyStringThrowsException() {
        assertThrows(InvalidRegistrationNumberException.class, () -> new RegistrationNumber(""));
    }

    @Test
    void ensureTooShortSuffixThrowsException() {
        assertThrows(InvalidRegistrationNumberException.class, () -> new RegistrationNumber("CS-TP"));
    }

    @Test
    void ensureMissingHyphenThrowsException() {
        assertThrows(InvalidRegistrationNumberException.class, () -> new RegistrationNumber("CSTPA"));
    }

    @Test
    void ensureDigitInPrefixThrowsException() {
        assertThrows(InvalidRegistrationNumberException.class, () -> new RegistrationNumber("1S-TPA"));
    }

    @Test
    void ensureEqualRegistrationNumbersAreEqual() {
        RegistrationNumber reg1 = new RegistrationNumber("CS-TPA");
        RegistrationNumber reg2 = new RegistrationNumber("CS-TPA");
        assertEquals(reg1, reg2);
    }

    @Test
    void ensureDifferentRegistrationNumbersAreNotEqual() {
        RegistrationNumber reg1 = new RegistrationNumber("CS-TPA");
        RegistrationNumber reg2 = new RegistrationNumber("CS-TPB");
        assertNotEquals(reg1, reg2);
    }

    @Test
    void ensureSameReferenceIsEqual() {
        RegistrationNumber reg = new RegistrationNumber("CS-TPA");
        assertEquals(reg, reg);
    }

    @Test
    void ensureEqualsReturnsFalseForNull() {
        RegistrationNumber reg = new RegistrationNumber("CS-TPA");
        assertNotEquals(reg, null);
    }

    @Test
    void ensureEqualsReturnsFalseForDifferentClass() {
        RegistrationNumber reg = new RegistrationNumber("CS-TPA");
        assertNotEquals(reg, "CS-TPA");
    }

    @Test
    void ensureHashCodeIsConsistentWithEquals() {
        RegistrationNumber reg1 = new RegistrationNumber("CS-TPA");
        RegistrationNumber reg2 = new RegistrationNumber("CS-TPA");
        assertEquals(reg1.hashCode(), reg2.hashCode());
    }

    @Test
    void ensureToStringReturnsNumber() {
        RegistrationNumber reg = new RegistrationNumber("CS-TPA");
        assertEquals("CS-TPA", reg.toString());
    }
}
