package aisafe.aircrafts.application.dtos;

/**
 * Read model returned when listing aircraft.
 */
public record ListAircraftsUseCaseResponse(
        String registrationNumber,
        String model,
        String status) {
}
