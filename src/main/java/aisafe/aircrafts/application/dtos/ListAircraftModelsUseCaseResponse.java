package aisafe.aircrafts.application.dtos;

/**
 * Response DTO for listing aircraft models.
 * Carries the essential fields displayed in the UI and returned by the API.
 */
public record ListAircraftModelsUseCaseResponse(
        Long Id,
        String modelName,
        String manufacturer,
        Integer maximumSeatingCapacity
) {}
