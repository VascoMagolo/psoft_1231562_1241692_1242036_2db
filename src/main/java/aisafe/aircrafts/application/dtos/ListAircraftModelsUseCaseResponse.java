package aisafe.aircrafts.application.dtos;

import aisafe.aircrafts.domain.Manufacturer;

/**
 * Response DTO for listing aircraft models.
 * Carries the essential fields displayed in the UI and returned by the API.
 */
public record ListAircraftModelsUseCaseResponse(
        String modelName,
        String manufacturer,
        Integer maximumSeatingCapacity
) {}
