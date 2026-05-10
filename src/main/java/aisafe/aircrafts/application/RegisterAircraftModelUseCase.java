package aisafe.aircrafts.application;

import aisafe.UseCase;
import aisafe.aircrafts.application.dtos.RegisterAircraftModelRequest; // <-- DTO CORRETO!
import aisafe.aircrafts.domain.*;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@Transactional
public class RegisterAircraftModelUseCase {

    private final AircraftModelRepository repository;

    public RegisterAircraftModelUseCase(AircraftModelRepository repository) {
        this.repository = repository;
    }

    public AircraftModel execute(RegisterAircraftModelRequest request) {
        AircraftModel newModel = new AircraftModel(
                request.modelName(),
                request.manufacturer(),
                request.fuelCapacity(),
                request.maxRange(),
                request.cruisingSpeed(),
                request.imagePath(),
                request.maximumSeatingCapacity()
        );

        return repository.save(newModel);
    }
}