package aisafe.aircrafts.application.dtos;

import aisafe.aircrafts.domain.Aircraft;

/**
 * Read model returned by the aircraft search use case.
 */
public record SearchAircraftUseCaseResponse(
        String registrationNumber,
        String model,
        String status,
        int manufacturingYear
) {
    public static SearchAircraftUseCaseResponse from(Aircraft aircraft) {
        return new SearchAircraftUseCaseResponse(
                aircraft.getRegistrationNumber().getNumber(),
                aircraft.getModel().getModelName(),
                aircraft.getStatus().name(),
                aircraft.getManufacturingDate() != null ? aircraft.getManufacturingDate().getYear() : null
        );
    }
}
