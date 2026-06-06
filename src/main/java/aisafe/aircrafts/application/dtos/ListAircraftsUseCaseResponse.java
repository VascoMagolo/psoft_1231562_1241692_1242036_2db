package aisafe.aircrafts.application.dtos;

import aisafe.aircrafts.domain.Aircraft;

/**
 * Read model returned when listing aircraft.
 */
public record ListAircraftsUseCaseResponse(
        String registrationNumber,
        String model,
        String status) {
    public static ListAircraftsUseCaseResponse from(Aircraft aircraft) {
        return new ListAircraftsUseCaseResponse(
                aircraft.getRegistrationNumber().getNumber(),
                aircraft.getModel().getModelName(),
                aircraft.getStatus().name()
        );
    }
}
