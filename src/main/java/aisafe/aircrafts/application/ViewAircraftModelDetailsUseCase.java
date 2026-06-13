package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.AircraftModelResponse;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelNotFoundException;
import aisafe.aircrafts.domain.AircraftModelRepository;
import aisafe.shared.application.UseCase;
import org.springframework.transaction.annotation.Transactional;

@UseCase(readOnly = true)
@Transactional(readOnly = true)
public class ViewAircraftModelDetailsUseCase {

    private final AircraftModelRepository repository;

    public ViewAircraftModelDetailsUseCase(AircraftModelRepository repository) {
        this.repository = repository;
    }

    public AircraftModelResponse execute(String modelName) {
        AircraftModel model = repository.findByModelName(modelName)
                .orElseThrow(() -> new AircraftModelNotFoundException("Aircraft model '" + modelName + "' not found."));

        return AircraftModelResponse.from(model);
    }
}