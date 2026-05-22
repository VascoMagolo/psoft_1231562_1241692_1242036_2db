package aisafe.aircrafts.application;

import aisafe.UseCase;
import aisafe.aircrafts.application.dtos.RegisterAircraftRequest;
import aisafe.aircrafts.application.dtos.ViewAircraftDetailsResponse;
import aisafe.aircrafts.domain.*;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@Transactional
public class RegisterAircraftUseCase {

    private final AircraftRepository aircraftRepository;
    private final AircraftModelRepository modelRepository;

    public RegisterAircraftUseCase(AircraftRepository aircraftRepository, AircraftModelRepository modelRepository) {
        this.aircraftRepository = aircraftRepository;
        this.modelRepository = modelRepository;
    }

    public ViewAircraftDetailsResponse execute(RegisterAircraftRequest request) {
        AircraftModel model = modelRepository.findById(request.modelId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Model ID"));

        RegistrationNumber regNum = new RegistrationNumber(request.registrationNumber());

        if (aircraftRepository.existsByRegistrationNumber(regNum)) {
            throw new AircraftAlreadyExistsException("Registration number already exists");
        }
        Aircraft aircraft = new Aircraft(
                AircraftStatus.valueOf(request.status().toUpperCase()),
                request.manufacturingDate(),
                model,
                regNum,
                request.seatCapacity(),
                request.features()
        );
        if (aircraft.getSeatCapacity() > model.getMaximumSeatingCapacity()) {
            throw new IllegalArgumentException("Seat capacity cannot exceed model's maximum seating capacity");
        }
        Aircraft savedAircraft = aircraftRepository.save(aircraft);

        return new ViewAircraftDetailsResponse(
                savedAircraft.getRegistrationNumber().getNumber(),
                savedAircraft.getModel().getModelName(),
                savedAircraft.getModel().getManufacturer(),
                savedAircraft.getManufacturingDate(),
                savedAircraft.getStatus(),
                savedAircraft.getSeatCapacity(),
                savedAircraft.getFeatures(),
                savedAircraft.getVersion()
        );
    }
}