package aisafe.aircrafts.application;

import aisafe.UseCase;
import aisafe.aircrafts.domain.*;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@Transactional
public class RegisterAircraftModelUseCase {

    private final AircraftModelRepository repository;

    public RegisterAircraftModelUseCase(AircraftModelRepository repository) {
        this.repository = repository;
    }

    public AircraftModel execute(AircraftModel newModel) {
        if (newModel.getMaxRange() == null || newModel.getMaxRange() <= 0) {
            throw new AircraftModelInvalidFieldException("Max Range is invalid");
        }
        if (newModel.getModelName() == null) {
            throw new AircraftModelInvalidFieldException("Model name is required");
        }
        if (newModel.getFuelCapacity() == null || newModel.getFuelCapacity() <= 0) {
            throw new AircraftModelInvalidFieldException("Fuel Range is invalid");
        }
        if (newModel.getCruisingSpeed() == null || newModel.getCruisingSpeed() <= 0) {
            throw new AircraftModelInvalidFieldException("Cruising speed is invalid");
        }
        // manufacturer validation maybe be changed, maybe manufacturer is a VO (value-object)
        if (newModel.getManufacturer() == null ) {
            throw new AircraftModelInvalidFieldException("Manufacturer name is missing");
        }
        return repository.save(newModel);
    }
}