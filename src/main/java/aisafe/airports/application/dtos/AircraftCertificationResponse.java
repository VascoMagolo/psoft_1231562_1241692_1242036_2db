package aisafe.airports.application.dtos;

import aisafe.airports.domain.AircraftCertification;

/**
 * DTO representing the response for an aircraft certification.
 * @param airportIataCode the IATA code of the airport where the certification is valid
 * @param aircraftModelName the name of the certified aircraft model
 */
public record AircraftCertificationResponse(
        String airportIataCode,
        String aircraftModelName
) {
    public static AircraftCertificationResponse from(AircraftCertification certification) {
        return new AircraftCertificationResponse(
                certification.getAirport().getIataCode().getCode(),
                certification.getAircraftModelName()
        );
    }
}
