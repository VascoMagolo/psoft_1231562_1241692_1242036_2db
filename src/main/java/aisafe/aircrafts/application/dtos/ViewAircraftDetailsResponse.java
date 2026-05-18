package aisafe.aircrafts.application.dtos;

import aisafe.aircrafts.domain.AircraftStatus;
import aisafe.aircrafts.domain.Manufacturer;

import java.time.LocalDate;
import java.util.List;

/**
 * Detailed aircraft view returned when looking up an aircraft by registration number.
 */
public record ViewAircraftDetailsResponse(
        String registrationNumber,
        String model,
        Manufacturer manufacturer,
        LocalDate manufacturingDate,
        AircraftStatus status,
        Integer seatCapacity,
        List<String> features
) {
}
