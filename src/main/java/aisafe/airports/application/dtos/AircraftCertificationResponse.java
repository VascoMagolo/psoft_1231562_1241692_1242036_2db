package aisafe.airports.application.dtos;

import aisafe.airports.domain.AircraftCertification;

/**
 * DTO representing the response for an aircraft certification.
 * @param id the unique identifier of the aircraft certification
 * @param airportIataCode the IATA code of the airport where the certification is valid
 * @param aircraftModelId the unique identifier of the certified aircraft model
 * @param aircraftModelName the name of the certified aircraft model
 */
public record AircraftCertificationResponse(
        Long id,
        String airportIataCode,
        Long aircraftModelId,
        String aircraftModelName
) {
    /**
     * Factory method to create an AircraftCertificationResponse from an AircraftCertification domain object.
     * @param certification the AircraftCertification domain object to convert
     * @return a new AircraftCertificationResponse containing the relevant data from the domain object
     */
    public static AircraftCertificationResponse from(AircraftCertification certification) {
        return new AircraftCertificationResponse(
                certification.getId(),
                certification.getAirport().getIataCode().getCode(),
                certification.getAircraftModel().getId(),
                certification.getAircraftModel().getModelName()
        );
    }
}
