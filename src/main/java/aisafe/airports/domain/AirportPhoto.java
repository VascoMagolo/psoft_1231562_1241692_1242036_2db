package aisafe.airports.domain;

import java.util.Arrays;
import java.util.Objects;

public final class AirportPhoto {

    private final byte[] bytes;
    private final String contentType;

    public AirportPhoto(byte[] bytes, String contentType) {
        if (bytes == null || bytes.length == 0)
            throw new InvalidAirportException("photo bytes must not be empty");
        if (contentType == null || contentType.isBlank())
            throw new InvalidAirportException("photo content type must not be blank");
        this.bytes = bytes;
        this.contentType = contentType;
    }

    public byte[] getBytes() { return bytes; }
    public String getContentType() { return contentType; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AirportPhoto other)) return false;
        return Arrays.equals(bytes, other.bytes) && Objects.equals(contentType, other.contentType);
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(bytes) + Objects.hashCode(contentType);
    }

    @Override
    public String toString() {
        return "AirportPhoto{contentType='" + contentType + "', size=" + bytes.length + "}";
    }
}
