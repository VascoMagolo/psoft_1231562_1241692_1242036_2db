package aisafe.aircrafts.application.dtos;

/**
 * Response DTO for listing aircraft models.
 * Carries the essential fields displayed in the UI and returned by the API.
 */
public record ListAircraftModelsUseCaseResponse(
        Long id,
        String modelName,
        String manufacturer,
        Double fuelCapacity,
        Double maxRange,
        Double cruisingSpeed,
        Integer maximumSeatingCapacity,
        String imagePath
) {}

