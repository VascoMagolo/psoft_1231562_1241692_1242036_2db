package aisafe.aircrafts.application.dtos;

import aisafe.aircrafts.domain.AircraftStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Read model returned when listing aircraft.
 */
public record ListAircraftsUseCaseResponse(
        String registrationNumber,
        String model,
        String manufacturer,
        LocalDate manufacturingDate,
        AircraftStatus status,
        Integer seatCapacity,
        List<String> features) {
}
