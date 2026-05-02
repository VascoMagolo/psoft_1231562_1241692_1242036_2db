package aisafe.aircrafts.application;

import aisafe.UseCase;
import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@Transactional(readOnly = true)
public class ViewAircraftDetailsUseCase {
    private final AircraftRepository repository;

    public ViewAircraftDetailsUseCase(AircraftRepository repository) {
        this.repository = repository;
    }

    public Aircraft execute(RegistrationNumber registrationNumber) {
        return repository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new AircraftNotFoundException("Airplane with registration number: " + registrationNumber + " not found."));
    }
}
