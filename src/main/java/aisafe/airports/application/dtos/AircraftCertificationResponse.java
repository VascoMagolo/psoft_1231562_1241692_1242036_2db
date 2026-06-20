package aisafe.airports.application.dtos;

import aisafe.airports.domain.AircraftCertification;

public record AircraftCertificationResponse(
        String airportIataCode,
        String aircraftModelName
) {
    public static AircraftCertificationResponse from(AircraftCertification certification) {
        return new AircraftCertificationResponse(
                certification.getAirportCode().getCode(),
                certification.getAircraftModelName().getName()
        );
    }
}
