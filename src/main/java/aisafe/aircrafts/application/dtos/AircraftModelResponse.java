package aisafe.aircrafts.application.dtos;

import aisafe.aircrafts.domain.Manufacturer;

public record AircraftModelResponse(
        Long id,
        String modelName,
        Manufacturer manufacturer,
        Double fuelCapacity,
        Double maxRange,
        Double cruisingSpeed,
        String imagePath,
        Integer maximumSeatingCapacity
) {
}