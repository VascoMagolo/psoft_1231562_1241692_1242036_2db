package aisafe.aircrafts.application.dtos;

public record UpdateAircraftModelRequest(
        Double cruisingSpeed,
        Double fuelCapacity,
        Double maxRange,
        Integer maximumSeatingCapacity
) {}