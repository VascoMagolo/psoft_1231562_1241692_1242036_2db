package aisafe.aircrafts.application.dtos;

import aisafe.aircrafts.domain.Manufacturer;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response DTO representing an aircraft model.")
public record AircraftModelResponse(
        String modelName,
        Manufacturer manufacturer,
        Double fuelCapacity,
        Double maxRange,
        Double cruisingSpeed,
        String imagePath,
        Integer maximumSeatingCapacity
) {
}