package aisafe.aircrafts.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AircraftModelImageTest {

    @Test
    void ensureValidImageIsCreated() {
        byte[] bytes = new byte[]{1, 2, 3};
        AircraftModelImage image = new AircraftModelImage(bytes, "image/jpeg");
        assertArrayEquals(bytes, image.getBytes());
        assertEquals("image/jpeg", image.getContentType());
    }

    @Test
    void ensureNullBytesThrowsException() {
        assertThrows(AircraftInvalidFieldException.class, () ->
                new AircraftModelImage(null, "image/jpeg"));
    }

    @Test
    void ensureEmptyBytesThrowsException() {
        assertThrows(AircraftInvalidFieldException.class, () ->
                new AircraftModelImage(new byte[]{}, "image/jpeg"));
    }

    @Test
    void ensureNullContentTypeThrowsException() {
        assertThrows(AircraftInvalidFieldException.class, () ->
                new AircraftModelImage(new byte[]{1}, null));
    }

    @Test
    void ensureBlankContentTypeThrowsException() {
        assertThrows(AircraftInvalidFieldException.class, () ->
                new AircraftModelImage(new byte[]{1}, "  "));
    }

    @Test
    void ensureEqualityByValue() {
        byte[] bytes = new byte[]{1, 2, 3};
        AircraftModelImage a = new AircraftModelImage(bytes, "image/jpeg");
        AircraftModelImage b = new AircraftModelImage(bytes.clone(), "image/jpeg");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void ensureInequalityOnDifferentBytes() {
        AircraftModelImage a = new AircraftModelImage(new byte[]{1}, "image/jpeg");
        AircraftModelImage b = new AircraftModelImage(new byte[]{2}, "image/jpeg");
        assertNotEquals(a, b);
    }

    @Test
    void ensureInequalityOnDifferentContentType() {
        byte[] bytes = new byte[]{1, 2, 3};
        AircraftModelImage a = new AircraftModelImage(bytes, "image/jpeg");
        AircraftModelImage b = new AircraftModelImage(bytes.clone(), "image/png");
        assertNotEquals(a, b);
    }

    @Test
    void ensureToStringContainsContentTypeAndSize() {
        AircraftModelImage image = new AircraftModelImage(new byte[]{1, 2, 3}, "image/jpeg");
        String s = image.toString();
        assertTrue(s.contains("image/jpeg"));
        assertTrue(s.contains("3"));
    }
}
