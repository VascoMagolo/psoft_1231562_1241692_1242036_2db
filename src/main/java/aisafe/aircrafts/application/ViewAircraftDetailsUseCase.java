package aisafe.aircrafts.application;

import aisafe.UseCase;
import aisafe.aircrafts.application.dtos.ViewAircraftDetailsResponse;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for viewing the details of a specific aircraft.
 * The use case retrieves the aircraft by its registration number and returns a DTO with all the details
 */
@UseCase
@Transactional(readOnly = true)
public class ViewAircraftDetailsUseCase {

    private final AircraftRepository repository;

    public ViewAircraftDetailsUseCase(AircraftRepository repository) {
        this.repository = repository;
    }

    /**
     * Retrieves the details of an aircraft by its registration number.
     * @param registrationNumber the registration number of the aircraft to retrieve
     * @return a DTO containing the details of the aircraft
     */
    public ViewAircraftDetailsResponse execute(RegistrationNumber registrationNumber) {
        Aircraft aircraft = repository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft not found with registration: " + registrationNumber.getNumber()));

        return new ViewAircraftDetailsResponse(
                aircraft.getRegistrationNumber().getNumber(),
                aircraft.getModel().getModelName(),
                aircraft.getModel().getManufacturer(),
                aircraft.getManufacturingDate(),
                aircraft.getStatus(),
                aircraft.getSeatCapacity(),
                aircraft.getFeatures(),
                aircraft.getVersion()
        );
    }
}