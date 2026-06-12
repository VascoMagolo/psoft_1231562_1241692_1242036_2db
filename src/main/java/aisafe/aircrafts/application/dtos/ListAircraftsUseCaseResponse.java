package aisafe.aircrafts.application.dtos;

import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftStatus;
import aisafe.aircrafts.domain.Manufacturer;

import java.time.LocalDate;
import java.util.List;

/**
 * Read model returned when listing aircraft.
 */
public record ListAircraftsUseCaseResponse(
        String registrationNumber,
        String model,
        Manufacturer manufacturer,
        LocalDate manufacturingDate,
        AircraftStatus status,
        Integer seatCapacity,
        Double range,
        List<String> features,
        Long version) {
    public static ListAircraftsUseCaseResponse from(Aircraft aircraft) {
        return new ListAircraftsUseCaseResponse(
                aircraft.getRegistrationNumber().getNumber(),
                aircraft.getModel().getModelName(),
                aircraft.getModel().getManufacturer(),
                aircraft.getManufacturingDate(),
                aircraft.getStatus(),
                aircraft.getSeatCapacity(),
                aircraft.getRange(),
                aircraft.getFeatures(),
                aircraft.getVersion()
        );
    }
}
