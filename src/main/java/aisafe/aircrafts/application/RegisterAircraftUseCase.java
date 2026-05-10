package aisafe.aircrafts.application;

import aisafe.UseCase;
import aisafe.aircrafts.application.dtos.RegisterAircraftRequest;
import aisafe.aircrafts.domain.*;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@Transactional
public class RegisterAircraftUseCase {
    private final AircraftRepository repository;
    private final AircraftModelRepository modelRepository;
    public RegisterAircraftUseCase(AircraftRepository repository, AircraftModelRepository modelRepository) {
        this.repository = repository;
        this.modelRepository = modelRepository;
    }

    public Aircraft execute(RegisterAircraftRequest request) {
        AircraftModel model = modelRepository.findByModelName(request.modelName())
                .orElseThrow(() -> new AircraftModelNotFoundException("Model '" + request.modelName() + "' not found."));
        Aircraft aircraft = new Aircraft(
            request.status(),
            request.manufacturingDate(),
            model,
            request.registrationNumber(),
            request.seatCapacity(),
            request.features()
        );
        return repository.save(aircraft);
    }
}