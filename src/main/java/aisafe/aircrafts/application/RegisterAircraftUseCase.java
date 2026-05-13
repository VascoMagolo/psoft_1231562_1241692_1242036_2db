package aisafe.aircrafts.application;

import aisafe.UseCase;
import aisafe.aircrafts.application.dtos.RegisterAircraftRequest;
import aisafe.aircrafts.domain.*;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registers a new aircraft after resolving its model, checking seat capacity limits, and ensuring the registration number is unique.
 */
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
        if (aircraft.getSeatCapacity() > model.getMaximumSeatingCapacity()) {
            throw new AircraftInvalidFieldException("Seat capacity cannot exceed the maximum seating capacity of the model.");
        }
        if (repository.existsByRegistrationNumber(aircraft.getRegistrationNumber())) {
            throw new AircraftAlreadyExistsException("An aircraft with registration number '" + aircraft.getRegistrationNumber() + "' already exists.");
        }
        return repository.save(aircraft);
    }
}