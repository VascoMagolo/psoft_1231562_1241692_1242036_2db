package aisafe.aircrafts.application;

import aisafe.shared.application.dtos.ImageData;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelImageNotFoundException;
import aisafe.aircrafts.domain.AircraftModelNotFoundException;
import aisafe.aircrafts.domain.AircraftModelRepository;
import aisafe.shared.application.UseCase;

@UseCase(readOnly = true)
public class GetAircraftModelImageUseCase {

    private final AircraftModelRepository repository;

    public GetAircraftModelImageUseCase(AircraftModelRepository repository) {
        this.repository = repository;
    }

    public ImageData execute(String modelName) {
        AircraftModel model = repository.findByModelName(modelName)
                .orElseThrow(() -> new AircraftModelNotFoundException(
                        "Aircraft model '" + modelName + "' not found."));

        if (model.getImage() == null) {
            throw new AircraftModelImageNotFoundException(
                    "Aircraft model '" + modelName + "' has no image.");
        }

        return new ImageData(model.getImage().getBytes(), model.getImage().getContentType());
    }
}
