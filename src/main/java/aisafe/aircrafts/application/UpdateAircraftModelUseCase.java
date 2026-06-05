package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.AircraftModelResponse;
import aisafe.aircrafts.application.dtos.UpdateAircraftModelRequest;
import aisafe.aircrafts.domain.*;
import aisafe.shared.application.UseCase;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for updating an existing aircraft model.
 */

@UseCase
@Transactional
public class UpdateAircraftModelUseCase {

    private final AircraftModelRepository aircraftModelRepository;

    public UpdateAircraftModelUseCase(AircraftModelRepository aircraftModelRepository) {
        this.aircraftModelRepository = aircraftModelRepository;
    }

    public AircraftModelResponse execute(String modelName, UpdateAircraftModelRequest request) {

        AircraftModel model = aircraftModelRepository.findByModelName(modelName)
                .orElseThrow(() -> new AircraftModelNotFoundException("Aircraft model '" + modelName + "' not found."));

        if (request.cruisingSpeed() != null) {
            model.setCruisingSpeed(request.cruisingSpeed());
        }
        if (request.fuelCapacity() != null) {
            model.setFuelCapacity(request.fuelCapacity());
        }
        if (request.maxRange() != null) {
            model.setMaxRange(request.maxRange());
        }
        if (request.maximumSeatingCapacity() != null) {
            model.setMaximumSeatingCapacity(request.maximumSeatingCapacity());
        }
        if (request.imagePath() != null && !request.imagePath().isBlank()) {
            model.setImagePath(request.imagePath());
        }
        aircraftModelRepository.save(model);

        return AircraftModelResponse.from(model);
    }
}