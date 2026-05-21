package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.ViewAircraftDetailsResponse;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.AircraftStatus;
import aisafe.aircrafts.domain.RegistrationNumber;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
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
}