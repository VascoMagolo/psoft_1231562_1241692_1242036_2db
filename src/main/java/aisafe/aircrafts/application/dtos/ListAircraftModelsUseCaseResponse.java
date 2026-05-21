package aisafe.aircrafts.application.dtos;

/**
 * Response DTO for listing aircraft models.
 * Carries the essential fields displayed in the UI and returned by the API.
 */
public record ListAircraftModelsUseCaseResponse(
        String modelName,
        String manufacturer,
        Integer maximumSeatingCapacity
) {}
