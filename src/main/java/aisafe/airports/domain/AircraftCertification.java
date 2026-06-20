package aisafe.airports.domain;

import aisafe.aircrafts.domain.ModelName;

public class AircraftCertification {

    private final IataCode airportCode;
    private final ModelName aircraftModelName;

    public AircraftCertification(IataCode airportCode, ModelName aircraftModelName) {
        if (airportCode == null) {
            throw new InvalidAircraftCertificationException("Airport code cannot be null.");
        }
        if (aircraftModelName == null) {
            throw new InvalidAircraftCertificationException("Aircraft model name cannot be null.");
        }
        this.airportCode = airportCode;
        this.aircraftModelName = aircraftModelName;
    }

    public IataCode getAirportCode() { return airportCode; }
    public ModelName getAircraftModelName() { return aircraftModelName; }
}
