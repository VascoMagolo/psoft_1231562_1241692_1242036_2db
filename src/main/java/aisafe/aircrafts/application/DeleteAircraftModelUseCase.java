package aisafe.aircrafts.application;

import aisafe.shared.application.UseCase;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelNotFoundException;
import aisafe.aircrafts.domain.AircraftModelRepository;

@UseCase
public class DeleteAircraftModelUseCase {
    private final AircraftModelRepository aircraftModelRepository;

    public DeleteAircraftModelUseCase(AircraftModelRepository aircraftModelRepository) {
        this.aircraftModelRepository = aircraftModelRepository;
    }

    public void execute(Long id) {
        AircraftModel model = aircraftModelRepository.findById(id)
                .orElseThrow(() -> new AircraftModelNotFoundException("Aircraft model not found with id: " + id));
        aircraftModelRepository.delete(model);
    }
}
