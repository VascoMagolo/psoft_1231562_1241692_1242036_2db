package aisafe.airports.domain;

import aisafe.aircrafts.domain.ModelName;

public class AircraftCertification {

    private final Airport airport;
    private final ModelName aircraftModelName;

    public AircraftCertification(Airport airport, ModelName aircraftModelName) {
        if (airport == null) {
            throw new InvalidAircraftCertificationException("Airport cannot be null.");
        }
        if (aircraftModelName == null) {
            throw new InvalidAircraftCertificationException("Aircraft model name cannot be null.");
        }
        this.airport = airport;
        this.aircraftModelName = aircraftModelName;
    }

    public Airport getAirport() { return airport; }
    public ModelName getAircraftModelName() { return aircraftModelName; }
}
