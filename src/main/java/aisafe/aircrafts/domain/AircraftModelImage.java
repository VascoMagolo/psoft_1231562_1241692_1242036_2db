package aisafe.aircrafts.domain;

import java.util.Arrays;
import java.util.Objects;

public final class AircraftModelImage {

    private final byte[] bytes;
    private final String contentType;

    public AircraftModelImage(byte[] bytes, String contentType) {
        if (bytes == null || bytes.length == 0)
            throw new AircraftInvalidFieldException("image bytes must not be empty");
        if (contentType == null || contentType.isBlank())
            throw new AircraftInvalidFieldException("image content type must not be blank");
        this.bytes = bytes;
        this.contentType = contentType;
    }

    public byte[] getBytes() { return bytes; }
    public String getContentType() { return contentType; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AircraftModelImage other)) return false;
        return Arrays.equals(bytes, other.bytes) && Objects.equals(contentType, other.contentType);
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(bytes) + Objects.hashCode(contentType);
    }

    @Override
    public String toString() {
        return "AircraftModelImage{contentType='" + contentType + "', size=" + bytes.length + "}";
    }
}
