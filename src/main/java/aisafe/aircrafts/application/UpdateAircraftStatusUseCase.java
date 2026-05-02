package aisafe.aircrafts.application;

import aisafe.UseCase;
import aisafe.aircrafts.domain.*;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@Transactional
public class UpdateAircraftStatusUseCase {
    private final AircraftRepository repository;

    public UpdateAircraftStatusUseCase(AircraftRepository repository) {
        this.repository = repository;
    }

    public Aircraft execute(RegistrationNumber registrationNumber, String newStatus) {
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new AircraftInvalidFieldException("Status cannot be empty.");
        }
        AircraftStatus parsedStatus;
        try {
            parsedStatus = AircraftStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AircraftInvalidFieldException("Invalid status provided: " + newStatus + ". Valid values are: " + java.util.Arrays.toString(AircraftStatus.values()));
        }
        Aircraft aircraft = repository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new AircraftNotFoundException("Airplane with registration number: " + registrationNumber + " not found."));
        aircraft.setStatus(parsedStatus);
        return repository.save(aircraft);
    }
}
