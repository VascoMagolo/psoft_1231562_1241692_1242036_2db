package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.AircraftModelResponse;
import aisafe.aircrafts.application.dtos.UpdateAircraftModelImageRequest;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelImage;
import aisafe.aircrafts.domain.AircraftModelNotFoundException;
import aisafe.aircrafts.domain.AircraftModelRepository;
import aisafe.shared.application.UseCase;

@UseCase
public class UpdateAircraftModelImageUseCase {

    private final AircraftModelRepository repository;

    public UpdateAircraftModelImageUseCase(AircraftModelRepository repository) {
        this.repository = repository;
    }

    public AircraftModelResponse execute(UpdateAircraftModelImageRequest request) {
        AircraftModel model = repository.findByModelName(request.modelName())
                .orElseThrow(() -> new AircraftModelNotFoundException(
                        "Aircraft model '" + request.modelName() + "' not found."));

        model.updateDetails(null, null, null, null, new AircraftModelImage(request.image(), request.imageContentType()));
        repository.save(model);

        return AircraftModelResponse.from(model);
    }
}
