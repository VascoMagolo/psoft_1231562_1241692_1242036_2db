package aisafe.airports.domain;

import java.util.Objects;

public final class Coordinates {
    private final Double latitude;
    private final Double longitude;

    public Coordinates(Double latitude, Double longitude) {
        if (latitude == null) {
            throw new InvalidCoordinatesException("Latitude cannot be null.");
        }
        if (longitude == null) {
            throw new InvalidCoordinatesException("Longitude cannot be null.");
        }
        if (latitude < -90.0 || latitude > 90.0) {
            throw new InvalidCoordinatesException("Latitude must be between -90 and 90 degrees.");
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new InvalidCoordinatesException("Longitude must be between -180 and 180 degrees.");
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return Objects.equals(latitude, that.latitude) && Objects.equals(longitude, that.longitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return "Coordinates{latitude=" + latitude + ", longitude=" + longitude + "}";
    }
}
