package aisafe.aircrafts.application.dtos;

/**
 * Read model returned by the aircraft search use case.
 */
public record SearchAircraftUseCaseResponse(
        String registrationNumber,
        String model,
        String status,
        int manufacturingYear
) {}
