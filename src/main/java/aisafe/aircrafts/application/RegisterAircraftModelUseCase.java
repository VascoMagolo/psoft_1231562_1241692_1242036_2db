package aisafe.aircrafts.application;

import aisafe.UseCase;
import aisafe.aircrafts.application.dtos.RegisterAircraftModelRequest; // <-- DTO CORRETO!
import aisafe.aircrafts.domain.*;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registers a new aircraft model after ensuring the model name is unique.
 */
@UseCase
@Transactional
public class RegisterAircraftModelUseCase {

    private final AircraftModelRepository repository;

    public RegisterAircraftModelUseCase(AircraftModelRepository repository) {
        this.repository = repository;
    }

    public AircraftModel execute(RegisterAircraftModelRequest request) {
        AircraftModel newModel = new AircraftModel(
                request.modelName(),
                request.manufacturer(),
                request.fuelCapacity(),
                request.maxRange(),
                request.cruisingSpeed(),
                request.imagePath(),
                request.maximumSeatingCapacity()
        );
        if (repository.existsByModelName(newModel.getModelName())) {
            throw new AircraftModelAlreadyExistsException("An aircraft model with name '" + newModel.getModelName() + "' already exists.");
        }
        return repository.save(newModel);
    }
}