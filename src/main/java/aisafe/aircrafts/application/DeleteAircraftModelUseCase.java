package aisafe.aircrafts.application;

import aisafe.shared.application.UseCase;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelNotFoundException;
import aisafe.aircrafts.domain.AircraftModelRepository;

/**
 * Use case for deleting an existing aircraft model from the system.
 */
@UseCase
public class DeleteAircraftModelUseCase {
    private final AircraftModelRepository aircraftModelRepository;

    public DeleteAircraftModelUseCase(AircraftModelRepository aircraftModelRepository) {
        this.aircraftModelRepository = aircraftModelRepository;
    }

    /**
     * Deletes the aircraft model with the specified name from the system.
     * @param modelName The name of the aircraft model to delete.
     */
    public void execute(String modelName) {
        AircraftModel model = aircraftModelRepository.findByModelName(modelName)
                .orElseThrow(() -> new AircraftModelNotFoundException("Aircraft model not found with name " + modelName));
        aircraftModelRepository.delete(model);
    }
}
