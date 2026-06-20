package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.UpdateAircraftRequest;
import aisafe.aircrafts.application.dtos.AircraftResponse;
import aisafe.aircrafts.domain.*;
import aisafe.shared.application.UseCase;
import aisafe.shared.domain.ConcurrencyException;

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

    public AircraftResponse execute(RegistrationNumber registration, UpdateAircraftRequest request, Long clientVersion) {

        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(registration)
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft with registration " + registration.getNumber() + " not found."));

        Long currentVersion = aircraftRepository.findVersionFor(registration);
        if (currentVersion != null && !currentVersion.equals(clientVersion)) {
            throw new ConcurrencyException("The aircraft has been updated by another user. Please refresh and try again.");
        }

        AircraftModel targetModel = aircraft.getModel();
        if (request.modelName() != null && !request.modelName().isBlank()) {
            targetModel = aircraftModelRepository.findByModelName(request.modelName())
                    .orElseThrow(() -> new AircraftModelNotFoundException("Aircraft model '" + request.modelName() + "' not found."));
        }

        AircraftStatus targetStatus = aircraft.getStatus();
        if (request.status() != null && !request.status().isBlank()) {
            if (!AircraftStatus.isValid(request.status())) {
                throw new AircraftInvalidFieldException("Invalid status value: " + request.status());
            }
            targetStatus = AircraftStatus.valueOf(request.status().toUpperCase());
        }

        aircraft.updateDetails(
                targetModel,
                request.manufacturingDate(),
                request.seatCapacity(),
                request.range(),
                request.features(),
                targetStatus
        );

        aircraftRepository.save(aircraft);

        Long newVersion = aircraftRepository.findVersionFor(registration);

        return AircraftResponse.from(aircraft, newVersion);
    }
}