package aisafe.aircrafts.application;

import aisafe.shared.application.UseCase;
import aisafe.aircrafts.application.dtos.AircraftModelResponse;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelRepository;

import aisafe.shared.domain.PaginatedResult;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Returns all stored aircraft models for the aircraft management screens and APIs.
 */
@UseCase(readOnly = true)
public class ListAircraftModelsUseCase {

    private final AircraftModelRepository repository;
    public ListAircraftModelsUseCase(AircraftModelRepository repository) {
        this.repository = repository;
    }

    /**
     * Return all aircraft models as lightweight DTOs used by the API/UI.
     * @param pageNumber the current page index
     * @param pageSize the number of items per page
     * @return a plain Java List of aircraft model DTOs
     */
    public PaginatedResult<AircraftModelResponse> execute(int pageNumber, int pageSize) {

        PaginatedResult<AircraftModel> modelsResult = repository.findAll(pageNumber, pageSize);

        List<AircraftModelResponse> data = modelsResult.data().stream()
                .map(AircraftModelResponse::from)
                .collect(Collectors.toList());

        return new PaginatedResult<>(data, modelsResult.totalElements());
    }
}