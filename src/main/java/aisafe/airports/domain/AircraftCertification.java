package aisafe.airports.domain;

import org.springframework.util.Assert;

/**
 * Represents the certification of an aircraft model at a specific airport.
 * Each certification indicates that a particular aircraft model is approved for operation at the associated airport.
 */
public class AircraftCertification {

    private final Airport airport;
    private final String aircraftModelName;

    public AircraftCertification(Airport airport, String aircraftModelName) {
        Assert.notNull(airport, "Airport cannot be null");
        Assert.hasText(aircraftModelName, "Aircraft model name cannot be blank");
        this.airport = airport;
        this.aircraftModelName = aircraftModelName;
    }

    public Airport getAirport() { return airport; }
    public String getAircraftModelName() { return aircraftModelName; }
}
