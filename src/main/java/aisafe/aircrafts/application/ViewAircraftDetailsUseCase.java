package aisafe.aircrafts.application;

import aisafe.shared.application.UseCase;
import aisafe.aircrafts.application.dtos.ViewAircraftDetailsRequest;
import aisafe.aircrafts.application.dtos.AircraftResponse;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;

/**
 * Use case for viewing the details of a specific aircraft.
 * The use case retrieves the aircraft by its registration number and returns a DTO with all the details
 */
@UseCase(readOnly = true)
public class ViewAircraftDetailsUseCase {

    private final AircraftRepository repository;

    public ViewAircraftDetailsUseCase(AircraftRepository repository) {
        this.repository = repository;
    }

    /**
     * Retrieves the details of an aircraft by its registration number.
     * @param request the request containing registration number of the aircraft to retrieve
     * @return a DTO containing the details of the aircraft
     */
    public AircraftResponse execute(ViewAircraftDetailsRequest request) {
        RegistrationNumber registrationNumber = request.registrationNumber();
        Aircraft aircraft = repository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft not found with registration: " + registrationNumber.getNumber()));

        Long version = repository.findVersionFor(registrationNumber);
        return AircraftResponse.from(aircraft, version);
    }
}