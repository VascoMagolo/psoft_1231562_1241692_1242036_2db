package aisafe.aircrafts.application.dtos;

/**
 * DTO for updating an existing aircraft model in the system.
 * @param cruisingSpeed The cruising speed of the aircraft model in knots.
 * @param fuelCapacity The fuel capacity of the aircraft model in liters.
 * @param maxRange The maximum range of the aircraft model in nautical miles.
 * @param maximumSeatingCapacity The maximum seating capacity of the aircraft model.
 * @param imagePath The file path to an image representing the aircraft model.
 */
public record UpdateAircraftModelRequest(
        Double cruisingSpeed,
        Double fuelCapacity,
        Double maxRange,
        Integer maximumSeatingCapacity,
        String imagePath
) {}