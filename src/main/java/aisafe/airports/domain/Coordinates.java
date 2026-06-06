package aisafe.airports.domain;

import org.springframework.util.Assert;

/**
 * Embeddable class representing geographical coordinates
 */
public class Coordinates {
    private final Double latitude;
    private final Double longitude;

    public Coordinates(Double latitude, Double longitude) {
        Assert.notNull(latitude, "Latitude cannot be null.");
        Assert.notNull(longitude, "Longitude cannot be null.");
        Assert.isTrue(latitude >= -90.0 && latitude <= 90.0, "Latitude must be between -90 and 90 degrees.");
        Assert.isTrue(longitude >= -180.0 && longitude <= 180.0, "Longitude must be between -180 and 180 degrees.");
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
}
