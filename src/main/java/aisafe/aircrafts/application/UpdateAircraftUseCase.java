package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.UpdateAircraftRequest;
import aisafe.aircrafts.application.dtos.ViewAircraftDetailsResponse;
import aisafe.aircrafts.domain.*;
import aisafe.shared.application.UseCase;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

/**
 * Use case for updating the details of an existing aircraft in the system.
 */
@UseCase
public class UpdateAircraftUseCase {

    private final AircraftRepository aircraftRepository;
    private final AircraftModelRepository aircraftModelRepository;

    public UpdateAircraftUseCase(AircraftRepository aircraftRepository, AircraftModelRepository aircraftModelRepository) {
        this.aircraftRepository = aircraftRepository;
        this.aircraftModelRepository = aircraftModelRepository;
    }

    public ViewAircraftDetailsResponse execute(RegistrationNumber registration, UpdateAircraftRequest request, Long clientVersion) {

        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(registration)
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft with registration " + registration.getNumber() + " not found."));

        Long currentVersion = aircraftRepository.findVersionFor(registration);
        if (!currentVersion.equals(clientVersion)) {
            throw new ObjectOptimisticLockingFailureException(Aircraft.class, registration.getNumber());
        }

        if (request.modelName() != null && !request.modelName().isBlank()) {
            AircraftModel newModel = aircraftModelRepository.findByModelName(request.modelName())
                    .orElseThrow(() -> new AircraftModelNotFoundException("Aircraft model '" + request.modelName() + "' not found."));
            aircraft.setModel(newModel);
        }

        if (request.manufacturingDate() != null) {
            aircraft.setManufacturingDate(request.manufacturingDate());
        }

        if (request.seatCapacity() != null) {
            if (request.seatCapacity() > aircraft.getModel().getMaximumSeatingCapacity()) {
                throw new AircraftInvalidFieldException("Seat capacity cannot exceed the model's maximum seating capacity (" + aircraft.getModel().getMaximumSeatingCapacity() + ").");
            }
            aircraft.setSeatCapacity(request.seatCapacity());
        }

        if (request.range() != null) {
            aircraft.setRange(request.range());
        }

        if (request.features() != null) {
            aircraft.setFeatures(request.features());
        }

        aircraftRepository.save(aircraft);

        Long newVersion = aircraftRepository.findVersionFor(registration);
        return ViewAircraftDetailsResponse.from(aircraft, newVersion);
    }
}