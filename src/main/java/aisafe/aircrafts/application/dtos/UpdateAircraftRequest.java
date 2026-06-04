package aisafe.aircrafts.application.dtos;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for updating an existing aircraft's details.
 * @param modelName The name of the aircraft model to update
 * @param manufacturingDate The new manufacturing date of the aircraft
 * @param seatCapacity The new seat capacity of the aircraft
 * @param features The new list of features for the aircraft
 */
public record UpdateAircraftRequest(
        String modelName,
        LocalDate manufacturingDate,
        Integer seatCapacity,
        List<String> features
) {}