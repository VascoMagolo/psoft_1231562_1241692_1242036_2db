package aisafe.aircrafts.application;

import aisafe.DomainException;
import aisafe.UseCase;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelRepository;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@Transactional
public class RegisterAircraftModelUseCase {

    private final AircraftModelRepository repository;

    public RegisterAircraftModelUseCase(AircraftModelRepository repository) {
        this.repository = repository;
    }

    public AircraftModel execute(AircraftModel newModel) {
        // add more validations later
        if (newModel.getMaxRange() == null || newModel.getMaxRange() <= 0) {
            throw new DomainException("Max Range is invalid");
        }

        return repository.save(newModel);
    }
}