package aisafe.airports.application.dtos;

import aisafe.airports.domain.AircraftCertification;

public record AircraftCertificationResponse(
        Long id,
        String airportIataCode,
        Long aircraftModelId,
        String aircraftModelName
) {
    public static AircraftCertificationResponse from(AircraftCertification certification) {
        return new AircraftCertificationResponse(
                certification.getId(),
                certification.getAirport().getIataCode().getCode(),
                certification.getAircraftModel().getId(),
                certification.getAircraftModel().getModelName()
        );
    }
}
