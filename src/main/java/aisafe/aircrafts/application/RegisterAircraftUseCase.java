package aisafe.aircrafts.application;

import aisafe.shared.application.UseCase;
import aisafe.aircrafts.application.dtos.RegisterAircraftRequest;
import aisafe.aircrafts.application.dtos.ViewAircraftDetailsResponse;
import aisafe.aircrafts.domain.*;

/**
 * Use case for registering a new aircraft in the system.
 * Validates input data, checks for duplicates, and saves the new aircraft to the repository.
 * Returns a DTO with the details of the newly registered aircraft.
 */
@UseCase
public class RegisterAircraftUseCase {

    private final AircraftRepository aircraftRepository;
    private final AircraftModelRepository modelRepository;

    public RegisterAircraftUseCase(AircraftRepository aircraftRepository, AircraftModelRepository modelRepository) {
        this.aircraftRepository = aircraftRepository;
        this.modelRepository = modelRepository;
    }

    /**
     * Registers a new aircraft based on the provided request.
     * Validates that the registration number is unique, the model exists, and that the status and seat capacity are valid.
     * Then creates and saves the new aircraft.
     * @param request the details of the aircraft to register
     * @return a DTO containing the details of the newly registered aircraft
     */
    public ViewAircraftDetailsResponse execute(RegisterAircraftRequest request) {
        AircraftModel model = modelRepository.findByModelName(request.modelName())
                .orElseThrow(() -> new AircraftInvalidFieldException("Invalid Model Name: " + request.modelName()));

        RegistrationNumber regNum = new RegistrationNumber(request.registrationNumber());

        if (aircraftRepository.existsByRegistrationNumber(regNum)) {
            throw new AircraftAlreadyExistsException("Registration number already exists");
        }
        if (!AircraftStatus.isValid(request.status())) {
            throw new AircraftInvalidFieldException("Invalid status value: " + request.status());
        }
        if (request.seatCapacity() > model.getMaximumSeatingCapacity()) {
            throw new AircraftInvalidFieldException("Seat capacity cannot exceed model's maximum seating capacity");
        }
        Aircraft aircraft = new Aircraft(
                AircraftStatus.valueOf(request.status().toUpperCase()),
                request.manufacturingDate(),
                model,
                regNum,
                request.seatCapacity(),
                request.features()
        );
        aircraftRepository.save(aircraft, null);

        return new ViewAircraftDetailsResponse(
                aircraft.getRegistrationNumber().getNumber(),
                aircraft.getModel().getModelName(),
                aircraft.getModel().getManufacturer(),
                aircraft.getManufacturingDate(),
                aircraft.getStatus(),
                aircraft.getSeatCapacity(),
                aircraft.getFeatures(),
                aircraft.getVersion()
        );
    }
}