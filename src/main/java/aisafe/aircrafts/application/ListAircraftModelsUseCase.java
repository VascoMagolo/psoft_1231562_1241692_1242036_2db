package aisafe.aircrafts.application;

import aisafe.UseCase;
import aisafe.aircrafts.application.dtos.ListAircraftModelsUseCaseResponse;
import aisafe.aircrafts.domain.AircraftModelRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Returns all stored aircraft models for the aircraft management screens and APIs.
 */
@UseCase
@Transactional(readOnly = true)
public class ListAircraftModelsUseCase {

    private final AircraftModelRepository repository;
    public ListAircraftModelsUseCase(AircraftModelRepository repository) {
        this.repository = repository;
    }

    /**
     * Return all aircraft models as lightweight DTOs used by the API/UI.
     */
    public List<ListAircraftModelsUseCaseResponse> execute() {
        return repository.findAll().stream()
                .map(m -> new ListAircraftModelsUseCaseResponse(
                        m.getId(),
                        m.getModelName(),
                        m.getManufacturer(),
                        m.getFuelCapacity(),
                        m.getMaxRange(),
                        m.getCruisingSpeed(),
                        m.getMaximumSeatingCapacity(),
                        m.getImagePath()
                ))
                .toList();
    }
}