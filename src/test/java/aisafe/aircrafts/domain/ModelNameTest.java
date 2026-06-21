package aisafe.aircrafts.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModelNameTest {

    @Test
    void ensureValidModelNameIsCreated() {
        ModelName name = new ModelName("A320");
        assertEquals("A320", name.getName());
    }

    @Test
    void ensureWhitespaceIsTrimmed() {
        ModelName name = new ModelName("  A320  ");
        assertEquals("A320", name.getName());
    }

    @Test
    void ensureNullThrowsException() {
        assertThrows(AircraftModelInvalidFieldException.class, () -> new ModelName(null));
    }

    @Test
    void ensureBlankThrowsException() {
        assertThrows(AircraftModelInvalidFieldException.class, () -> new ModelName("  "));
    }

    @Test
    void ensureEqualModelNamesAreEqual() {
        assertEquals(new ModelName("A320"), new ModelName("A320"));
    }

    @Test
    void ensureDifferentModelNamesAreNotEqual() {
        assertNotEquals(new ModelName("A320"), new ModelName("B737"));
    }

    @Test
    void ensureHashCodeIsConsistentWithEquals() {
        assertEquals(new ModelName("A320").hashCode(), new ModelName("A320").hashCode());
    }

    @Test
    void ensureToStringReturnsName() {
        assertEquals("A320", new ModelName("A320").toString());
    }

    @Test
    void ensureEqualsReturnsFalseForDifferentClass() {
        ModelName modelName = new ModelName("A320");
        assertNotEquals(modelName, "A320");
    }

    @Test
    void ensureEqualsReturnsFalseForNull() {
        ModelName modelName = new ModelName("A320");
        assertNotEquals(modelName, null);
    }
}
