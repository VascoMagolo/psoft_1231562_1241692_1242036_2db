package aisafe.aircrafts.application;

import aisafe.UseCase;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftRepository;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@Transactional
public class RegisterAircraftUseCase {
    private final AircraftRepository repository;

    public RegisterAircraftUseCase(AircraftRepository repository) {
        this.repository = repository;
    }

    public Aircraft execute(Aircraft aircraft) {
        return repository.save(aircraft);
    }
}