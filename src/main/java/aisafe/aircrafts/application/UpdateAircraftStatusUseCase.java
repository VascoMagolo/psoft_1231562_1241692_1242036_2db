package aisafe.aircrafts.application;

import aisafe.UseCase;
import aisafe.aircrafts.application.dtos.ViewAircraftDetailsResponse;
import aisafe.aircrafts.domain.*;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for updating the status of an aircraft.
 * The use case validates that the provided status is valid and that the client version matches the current
 */
@UseCase
@Transactional(readOnly = true)
public class UpdateAircraftStatusUseCase {

    private final AircraftRepository repository;

    public UpdateAircraftStatusUseCase(AircraftRepository repository) {
        this.repository = repository;
    }

    /**
     * Updates the status of an aircraft.
     * Validates that the provided status is valid and that the client version matches the current version of the aircraft to prevent lost updates.
     * @param registration the registration number of the aircraft to update
     * @param status the new status to set for the aircraft
     * @param clientVersion the version of the aircraft data that the client has, used for optimistic locking
     * @return a DTO containing the updated aircraft details
     */
    @Transactional
    public ViewAircraftDetailsResponse execute(RegistrationNumber registration, String status, Long clientVersion) {
        Aircraft aircraft = repository.findByRegistrationNumber(registration)
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft not found with registration: " + registration.getNumber()));

        if (!aircraft.getVersion().equals(clientVersion)) {
            throw new org.springframework.orm.ObjectOptimisticLockingFailureException(Aircraft.class, aircraft.getId());
        }
        if (!AircraftStatus.isValid(status)) {
            throw new AircraftInvalidFieldException("Invalid status value: " + status);
        }
        aircraft.setStatus(AircraftStatus.valueOf(status.toUpperCase()));

        Aircraft updatedAircraft = repository.save(aircraft);

        return toDto(updatedAircraft);
    }

    /**
     * Converts an Aircraft entity to a ViewAircraftDetailsResponse DTO.
     * @param aircraft the Aircraft entity to convert
     * @return a ViewAircraftDetailsResponse DTO containing the details of the aircraft
     */
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
}