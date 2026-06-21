package aisafe.airports.domain;

import aisafe.airports.domain.InvalidIataCodeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IataCodeTest {

    @Test
    void ensureValidIataCodeIsCreated() {
        IataCode code = new IataCode("LIS");
        assertEquals("LIS", code.getCode());
    }

    @Test
    void ensureLowercaseIsNormalized() {
        IataCode code = new IataCode("lis");
        assertEquals("LIS", code.getCode());
    }

    @Test
    void ensureAlphanumericCodeIsAccepted() {
        assertDoesNotThrow(() -> new IataCode("1A2"));
    }

    @Test
    void ensureNullThrowsException() {
        assertThrows(InvalidIataCodeException.class, () -> new IataCode(null));
    }

    @Test
    void ensureEmptyStringThrowsException() {
        assertThrows(InvalidIataCodeException.class, () -> new IataCode(""));
    }

    @Test
    void ensureTooShortCodeThrowsException() {
        assertThrows(InvalidIataCodeException.class, () -> new IataCode("LI"));
    }

    @Test
    void ensureTooLongCodeThrowsException() {
        assertThrows(InvalidIataCodeException.class, () -> new IataCode("LISS"));
    }

    @Test
    void ensureSpecialCharactersThrowException() {
        assertThrows(InvalidIataCodeException.class, () -> new IataCode("L!S"));
    }

    @Test
    void ensureEqualCodesAreEqual() {
        assertEquals(new IataCode("LIS"), new IataCode("LIS"));
    }

    @Test
    void ensureDifferentCodesAreNotEqual() {
        assertNotEquals(new IataCode("LIS"), new IataCode("OPO"));
    }

    @Test
    void ensureHashCodeIsConsistentWithEquals() {
        assertEquals(new IataCode("LIS").hashCode(), new IataCode("LIS").hashCode());
    }

    @Test
    void ensureToStringReturnsCode() {
        assertEquals("LIS", new IataCode("LIS").toString());
    }

    @Test
    void ensureBlankStringThrowsException() {
        assertThrows(InvalidIataCodeException.class, () -> new IataCode("   "));
    }

    @Test
    void ensureSameReferenceIsEqual() {
        IataCode code = new IataCode("LIS");
        assertEquals(code, code);
    }

    @Test
    void ensureNullNotEqual() {
        IataCode code = new IataCode("LIS");
        assertFalse(code.equals(null));
    }

    @Test
    void ensureDifferentClassNotEqual() {
        IataCode code = new IataCode("LIS");
        assertFalse(code.equals("LIS"));
    }
}
