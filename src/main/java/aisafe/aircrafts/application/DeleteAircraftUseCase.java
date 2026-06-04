package aisafe.aircrafts.application;

import aisafe.shared.application.UseCase;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;

/**
 * Use case for deleting an existing aircraft from the system.
 */
@UseCase
public class DeleteAircraftUseCase {
    private final AircraftRepository aircraftRepository;

    public DeleteAircraftUseCase(AircraftRepository aircraftRepository) {
        this.aircraftRepository = aircraftRepository;
    }

    /**
     * Deletes the aircraft with the specified registration number from the system.
     * @param registration The registration number of the aircraft to delete.
     */
    public void execute(RegistrationNumber registration) {
        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(registration)
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft not found with registration: " + registration.getNumber()));
        aircraftRepository.delete(aircraft);
    }
}
