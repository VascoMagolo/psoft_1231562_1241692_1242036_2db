package aisafe.airports.domain;

public class AircraftCertification {

    private final Airport airport;
    private final String aircraftModelName;

    public AircraftCertification(Airport airport, String aircraftModelName) {
        if (airport == null) {
            throw new InvalidAircraftCertificationException("Airport cannot be null.");
        }
        if (aircraftModelName == null || aircraftModelName.trim().isEmpty()) {
            throw new InvalidAircraftCertificationException("Aircraft model name cannot be blank.");
        }
        this.airport = airport;
        this.aircraftModelName = aircraftModelName;
    }

    public Airport getAirport() { return airport; }
    public String getAircraftModelName() { return aircraftModelName; }
}
