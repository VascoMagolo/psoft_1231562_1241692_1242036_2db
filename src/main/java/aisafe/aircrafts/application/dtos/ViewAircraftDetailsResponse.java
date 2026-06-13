package aisafe.aircrafts.application.dtos;

import aisafe.aircrafts.domain.Aircraft;
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
        Double range,
        List<String> features,
        Long version
) {
    public static ViewAircraftDetailsResponse from(Aircraft aircraft, Long version) {
        return new ViewAircraftDetailsResponse(
                aircraft.getRegistrationNumber().getNumber(),
                aircraft.getModel().getModelName(),
                aircraft.getModel().getManufacturer(),
                aircraft.getManufacturingDate(),
                aircraft.getStatus(),
                aircraft.getSeatCapacity(),
                aircraft.getRange(),
                aircraft.getFeatures(),
                version
        );
    }
}
