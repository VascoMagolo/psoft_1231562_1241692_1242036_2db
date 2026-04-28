package aisafe.aircrafts.application;

import aisafe.UseCase;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.AircraftStatus;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@Transactional
public class UpdateAircraftStatusUseCase {
    private final AircraftRepository repository;

    public UpdateAircraftStatusUseCase(AircraftRepository repository) {
        this.repository = repository;
    }

    public Aircraft execute(String registrationNumber, AircraftStatus newStatus) {

        Aircraft aircraft = repository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new AircraftNotFoundException("Airplane with registration number: " + registrationNumber + " not found."));
        aircraft.setStatus(newStatus);
        return repository.save(aircraft);
    }
}
