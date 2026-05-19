package aisafe.aircrafts.application.dtos;

import aisafe.aircrafts.domain.Manufacturer;

/**
 * Request payload used to register a new aircraft model.
 */

public record RegisterAircraftModelRequest(
        String modelName,
        Manufacturer manufacturer,
        Double maxRange,
        Double fuelCapacity,
        Double cruisingSpeed,
        Integer maximumSeatingCapacity,
        String imagePath
) {}