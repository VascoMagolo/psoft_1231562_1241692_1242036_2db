package aisafe.aircrafts.application.dtos;

import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.Manufacturer;

/**
 * Response DTO for listing aircraft models.
 * Carries the essential fields displayed in the UI and returned by the API.
 */
public record ListAircraftModelsUseCaseResponse(
        String modelName,
        Manufacturer manufacturer,
        Double fuelCapacity,
        Double maxRange,
        Double cruisingSpeed,
        String imagePath,
        Integer maximumSeatingCapacity
) {
    public static ListAircraftModelsUseCaseResponse from(AircraftModel model) {
        return new ListAircraftModelsUseCaseResponse(
                model.getModelName(),
                model.getManufacturer(),
                model.getFuelCapacity(),
                model.getMaxRange(),
                model.getCruisingSpeed(),
                model.getImagePath(),
                model.getMaximumSeatingCapacity()
        );
    }
}
