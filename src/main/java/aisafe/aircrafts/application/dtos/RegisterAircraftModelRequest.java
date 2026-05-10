package aisafe.aircrafts.application.dtos;

public record RegisterAircraftModelRequest(
        String modelName,
        String manufacturer,
        Double maxRange,
        Double fuelCapacity,
        Double cruisingSpeed,
        Integer maximumSeatingCapacity,
        String imagePath
) {}