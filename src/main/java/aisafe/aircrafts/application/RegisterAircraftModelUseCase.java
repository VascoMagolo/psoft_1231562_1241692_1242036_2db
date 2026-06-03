package aisafe.aircrafts.application;

import aisafe.shared.application.UseCase;
import aisafe.aircrafts.application.dtos.AircraftModelResponse;
import aisafe.aircrafts.application.dtos.RegisterAircraftModelRequest;
import aisafe.aircrafts.domain.*;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for registering a new aircraft model. Validates that the model name is unique and then creates and saves the new model.
 * Returns a DTO with the details of the newly created model.
 */
@UseCase
@Transactional
public class RegisterAircraftModelUseCase {

    private final AircraftModelRepository repository;

    public RegisterAircraftModelUseCase(AircraftModelRepository repository) {
        this.repository = repository;
    }

    /**
     * Registers a new aircraft model based on the provided request. Validates that the model name is unique and then creates and saves the new model.
     * @param request the details of the aircraft model to register
     * @return a DTO containing the details of the newly registered aircraft model
     */
    public AircraftModelResponse execute(RegisterAircraftModelRequest request) {
        if (repository.existsByModelName(request.modelName())) {
            throw new AircraftModelAlreadyExistsException("An aircraft model with name '" + request.modelName() + "' already exists.");
        }

        AircraftModel newModel = new AircraftModel(
                request.modelName(),
                request.manufacturer(),
                request.fuelCapacity(),
                request.maxRange(),
                request.cruisingSpeed(),
                request.imagePath(),
                request.maximumSeatingCapacity()
        );

        repository.save(newModel);

        return new AircraftModelResponse(
                newModel.getModelName(),
                newModel.getManufacturer(),
                newModel.getFuelCapacity(),
                newModel.getMaxRange(),
                newModel.getCruisingSpeed(),
                newModel.getImagePath(),
                newModel.getMaximumSeatingCapacity()
        );
    }
}