package aisafe.aircrafts.application.dtos;

import aisafe.aircrafts.domain.AircraftStatus;
import aisafe.aircrafts.domain.RegistrationNumber;

import java.time.LocalDate;
import java.util.List;

public record RegisterAircraftRequest (List<String> features, String modelName, Integer seatCapacity, AircraftStatus status, LocalDate manufacturingDate, RegistrationNumber registrationNumber, Long id) {
}
