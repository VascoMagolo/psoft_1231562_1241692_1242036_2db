package aisafe.aircrafts.application;

import aisafe.UseCase;
import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.aircrafts.application.dtos.ViewAircraftDetailsResponse;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads a single aircraft by registration number and maps it to the detailed view response.
 */
@UseCase
@Transactional(readOnly = true)
public class ViewAircraftDetailsUseCase {
    private final AircraftRepository repository;

    public ViewAircraftDetailsUseCase(AircraftRepository repository) {
        this.repository = repository;
    }

    public ViewAircraftDetailsResponse execute(RegistrationNumber registrationNumber) {
        Aircraft aircraft = repository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft with registration number: " + registrationNumber + " not found."));
        return new ViewAircraftDetailsResponse(
                aircraft.getRegistrationNumber().getNumber(),
                aircraft.getModel().getModelName(),
                aircraft.getModel().getManufacturer(),
                aircraft.getManufacturingDate(),
                aircraft.getStatus(),
                aircraft.getSeatCapacity(),
                aircraft.getFeatures()
        );
    }
}
