package aisafe.aircrafts.application;

import aisafe.UseCase;
import aisafe.aircrafts.application.dtos.ListAircraftsUseCaseResponse;
import aisafe.aircrafts.domain.AircraftModel;
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
    private ListAircraftsUseCaseResponse response;
    public ListAircraftModelsUseCase(AircraftModelRepository repository) {
        this.repository = repository;
    }

    public List<AircraftModel> execute() {
        return repository.findAll();
    }
}