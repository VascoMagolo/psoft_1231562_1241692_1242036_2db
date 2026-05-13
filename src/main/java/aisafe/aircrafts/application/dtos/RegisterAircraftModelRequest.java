package aisafe.aircrafts.application.dtos;
/**
 * Request payload used to register a new aircraft model.
 */

public record RegisterAircraftModelRequest(
        String modelName,
        String manufacturer,
        Double maxRange,
        Double fuelCapacity,
        Double cruisingSpeed,
        Integer maximumSeatingCapacity,
        String imagePath
) {}