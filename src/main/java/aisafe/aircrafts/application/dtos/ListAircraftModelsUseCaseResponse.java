package aisafe.aircrafts.application.dtos;

import aisafe.aircrafts.domain.Manufacturer;

/**
 * Response DTO for listing aircraft models.
 * Carries the essential fields displayed in the UI and returned by the API.
 */
public record ListAircraftModelsUseCaseResponse(
        Long id,
        String modelName,
        Manufacturer manufacturer,
        Double fuelCapacity,
        Double maxRange,
        Double cruisingSpeed,
        Integer maximumSeatingCapacity,
        String imagePath
) {}

