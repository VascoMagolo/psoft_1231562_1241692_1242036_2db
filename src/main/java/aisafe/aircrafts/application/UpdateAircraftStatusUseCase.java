package aisafe.aircrafts.application;

import aisafe.shared.application.UseCase;
import aisafe.aircrafts.application.dtos.ViewAircraftDetailsResponse;
import aisafe.aircrafts.domain.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

/**
 * Use case for updating the status of an aircraft.
 */
@UseCase
public class UpdateAircraftStatusUseCase {

    private final AircraftRepository repository;

    public UpdateAircraftStatusUseCase(AircraftRepository repository) {
        this.repository = repository;
    }

    /**
     * Updates the status of an aircraft.
     * Delegates optimistic locking validation to the infrastructure layer.
     */
    public ViewAircraftDetailsResponse execute(RegistrationNumber registration, String status, Long clientVersion) {

        Aircraft aircraft = repository.findByRegistrationNumber(registration)
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft not found with registration: " + registration.getNumber()));

        Long currentVersion = repository.findVersionFor(registration);
        if (!currentVersion.equals(clientVersion)) {
            throw new ObjectOptimisticLockingFailureException(Aircraft.class, registration.getNumber());
        }

        if (!AircraftStatus.isValid(status)) {
            throw new AircraftInvalidFieldException("Invalid status value: " + status);
        }
        aircraft.changeStatus(AircraftStatus.valueOf(status.toUpperCase()));

        repository.save(aircraft);

        Long newVersion = repository.findVersionFor(registration);
        return ViewAircraftDetailsResponse.from(aircraft, newVersion);
    }
}