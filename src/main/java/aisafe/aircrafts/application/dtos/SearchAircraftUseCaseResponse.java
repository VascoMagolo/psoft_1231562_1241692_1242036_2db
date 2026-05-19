package aisafe.aircrafts.application.dtos;

import aisafe.aircrafts.domain.AircraftStatus;
import aisafe.aircrafts.domain.Manufacturer;

import java.time.LocalDate;
import java.util.List;

/**
 * Read model returned by the aircraft search use case.
 */
public record SearchAircraftUseCaseResponse(
        String registrationNumber,
        String model,
        Manufacturer manufacturer,
        LocalDate manufacturingDate,
        AircraftStatus status,
        Integer seatCapacity,
        List<String> features
    ){
}
