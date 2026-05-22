package aisafe.aircrafts.application;

import aisafe.UseCase;
import aisafe.aircrafts.application.dtos.ViewAircraftDetailsResponse;
import aisafe.aircrafts.domain.*;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@Transactional(readOnly = true)
public class UpdateAircraftStatusUseCase {

    private final AircraftRepository repository;

    public UpdateAircraftStatusUseCase(AircraftRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ViewAircraftDetailsResponse execute(RegistrationNumber registration, String status, Long clientVersion) {
        Aircraft aircraft = repository.findByRegistrationNumber(registration)
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft not found with registration: " + registration.getNumber()));

        if (!aircraft.getVersion().equals(clientVersion)) {
            throw new org.springframework.orm.ObjectOptimisticLockingFailureException(Aircraft.class, aircraft.getId());
        }
        if (!isValidStatus(status)) {
            throw new AircraftInvalidFieldException("Invalid status value: " + status);
        }
        aircraft.setStatus(AircraftStatus.valueOf(status.toUpperCase()));

        Aircraft updatedAircraft = repository.save(aircraft);

        return toDto(updatedAircraft);
    }

    private ViewAircraftDetailsResponse toDto(Aircraft aircraft) {
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

    /**
     * Validates if the provided status is a valid AircraftStatus enum value
     * @param status
     * @return
     */
    private boolean isValidStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        for (AircraftStatus validStatus : AircraftStatus.values()) {
            if (validStatus.name().equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }
}