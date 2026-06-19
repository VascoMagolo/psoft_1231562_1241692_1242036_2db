package aisafe.airports.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AirportPhotoTest {

    @Test
    void ensureValidPhotoIsCreated() {
        byte[] bytes = new byte[]{1, 2, 3};
        AirportPhoto photo = new AirportPhoto(bytes, "image/jpeg");
        assertArrayEquals(bytes, photo.getBytes());
        assertEquals("image/jpeg", photo.getContentType());
    }

    @Test
    void ensureNullBytesThrowsException() {
        assertThrows(InvalidAirportException.class, () ->
                new AirportPhoto(null, "image/jpeg"));
    }

    @Test
    void ensureEmptyBytesThrowsException() {
        assertThrows(InvalidAirportException.class, () ->
                new AirportPhoto(new byte[]{}, "image/jpeg"));
    }

    @Test
    void ensureNullContentTypeThrowsException() {
        assertThrows(InvalidAirportException.class, () ->
                new AirportPhoto(new byte[]{1}, null));
    }

    @Test
    void ensureBlankContentTypeThrowsException() {
        assertThrows(InvalidAirportException.class, () ->
                new AirportPhoto(new byte[]{1}, "  "));
    }

    @Test
    void ensureEqualityByValue() {
        byte[] bytes = new byte[]{1, 2, 3};
        AirportPhoto a = new AirportPhoto(bytes, "image/jpeg");
        AirportPhoto b = new AirportPhoto(bytes.clone(), "image/jpeg");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void ensureInequalityOnDifferentBytes() {
        AirportPhoto a = new AirportPhoto(new byte[]{1}, "image/jpeg");
        AirportPhoto b = new AirportPhoto(new byte[]{2}, "image/jpeg");
        assertNotEquals(a, b);
    }

    @Test
    void ensureInequalityOnDifferentContentType() {
        byte[] bytes = new byte[]{1, 2, 3};
        AirportPhoto a = new AirportPhoto(bytes, "image/jpeg");
        AirportPhoto b = new AirportPhoto(bytes.clone(), "image/png");
        assertNotEquals(a, b);
    }

    @Test
    void ensureToStringContainsContentTypeAndSize() {
        AirportPhoto photo = new AirportPhoto(new byte[]{1, 2, 3}, "image/jpeg");
        String s = photo.toString();
        assertTrue(s.contains("image/jpeg"));
        assertTrue(s.contains("3"));
    }
}
