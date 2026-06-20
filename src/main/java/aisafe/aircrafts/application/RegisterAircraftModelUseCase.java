package aisafe.aircrafts.application;

import aisafe.shared.application.UseCase;
import aisafe.aircrafts.application.dtos.AircraftModelResponse;
import aisafe.aircrafts.application.dtos.RegisterAircraftModelRequest;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelImage;
import aisafe.aircrafts.domain.AircraftModelRepository;
import aisafe.shared.domain.DuplicateResourceException;

/**
 * Use case for registering a new aircraft model. Validates that the model name is unique and then creates and saves the new model.
 * Returns a DTO with the details of the newly created model.
 */
@UseCase
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
            throw new DuplicateResourceException("An aircraft model with name '" + request.modelName() + "' already exists.");
        }

        AircraftModelImage image = request.image() != null
                ? new AircraftModelImage(request.image(), request.imageContentType())
                : null;

        AircraftModel newModel = new AircraftModel(
                request.modelName(),
                request.manufacturer(),
                request.fuelCapacity(),
                request.maxRange(),
                request.cruisingSpeed(),
                image,
                request.maximumSeatingCapacity()
        );

        repository.save(newModel);

        return AircraftModelResponse.from(newModel);
    }
}