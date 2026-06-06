package aisafe.aircrafts.application;

import aisafe.shared.application.UseCase;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelNotFoundException;
import aisafe.aircrafts.domain.AircraftModelRepository;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.shared.domain.ResourceInUseException;

/**
 * Use case for deleting an existing aircraft model from the system.
 */
@UseCase
public class DeleteAircraftModelUseCase {
    private final AircraftModelRepository aircraftModelRepository;
    private final AircraftRepository aircraftRepository;

    public DeleteAircraftModelUseCase(AircraftModelRepository aircraftModelRepository, AircraftRepository aircraftRepository) {
        this.aircraftModelRepository = aircraftModelRepository;
        this.aircraftRepository = aircraftRepository;
    }

    /**
     * Deletes the aircraft model with the specified name from the system.
     * @param modelName The name of the aircraft model to delete.
     */
    public void execute(String modelName) {
        AircraftModel model = aircraftModelRepository.findByModelName(modelName)
                .orElseThrow(() -> new AircraftModelNotFoundException("Aircraft model not found with name " + modelName));

        if (aircraftRepository.anyAircraftExistsForModel(modelName)) {
            throw new ResourceInUseException("Cannot delete aircraft model because there are aircraft associated with it.");
        }

        aircraftModelRepository.delete(model);
    }
}
