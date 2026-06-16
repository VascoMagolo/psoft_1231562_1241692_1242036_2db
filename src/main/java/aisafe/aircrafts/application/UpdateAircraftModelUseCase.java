package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.AircraftModelResponse;
import aisafe.aircrafts.application.dtos.UpdateAircraftModelRequest;
import aisafe.aircrafts.domain.*;
import aisafe.shared.application.UseCase;

/**
 * Use case for updating an existing aircraft model.
 */

@UseCase
public class UpdateAircraftModelUseCase {

    private final AircraftModelRepository aircraftModelRepository;

    public UpdateAircraftModelUseCase(AircraftModelRepository aircraftModelRepository) {
        this.aircraftModelRepository = aircraftModelRepository;
    }

    public AircraftModelResponse execute(String modelName, UpdateAircraftModelRequest request) {

        AircraftModel model = aircraftModelRepository.findByModelName(modelName)
                .orElseThrow(() -> new AircraftModelNotFoundException("Aircraft model '" + modelName + "' not found."));

        model.updateDetails(
                request.fuelCapacity(),
                request.maxRange(),
                request.cruisingSpeed(),
                request.maximumSeatingCapacity(),
                request.imagePath()
        );
        
        aircraftModelRepository.save(model);

        return AircraftModelResponse.from(model);
    }
}