package aisafe.aircrafts.application;

import aisafe.UseCase;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.application.dtos.ListAircraftsUseCaseResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Returns all registered aircraft as lightweight response DTOs.
 */
@UseCase
@Transactional(readOnly = true)
public class ListAircraftUseCase {
    private final AircraftRepository repository;

    public ListAircraftUseCase(AircraftRepository repository) {
        this.repository = repository;
    }

    public List<ListAircraftsUseCaseResponse> execute() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private ListAircraftsUseCaseResponse toResponse(Aircraft aircraft) {
        return new ListAircraftsUseCaseResponse(
                aircraft.getRegistrationNumber().getNumber(),
                aircraft.getModel().getModelName(),
                aircraft.getModel().getManufacturer(),
                aircraft.getManufacturingDate(),
                aircraft.getStatus(),
                aircraft.getSeatCapacity(),
                aircraft.getFeatures()
        );
    }
}