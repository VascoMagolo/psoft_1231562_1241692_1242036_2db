package aisafe.aircrafts.application;

import aisafe.shared.application.UseCase;
import aisafe.aircrafts.application.dtos.ListAircraftModelsUseCaseResponse;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelRepository;
import org.springframework.transaction.annotation.Transactional;

import aisafe.shared.domain.PaginatedResult;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Returns all stored aircraft models for the aircraft management screens and APIs.
 */
@UseCase(readOnly = true)
@Transactional(readOnly = true)
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
    public PaginatedResult<ListAircraftModelsUseCaseResponse> execute(int pageNumber, int pageSize) {

        PaginatedResult<AircraftModel> modelsResult = repository.findAll(pageNumber, pageSize);

        List<ListAircraftModelsUseCaseResponse> data = modelsResult.data().stream()
                .map(ListAircraftModelsUseCaseResponse::from)
                .collect(Collectors.toList());

        return new PaginatedResult<>(data, modelsResult.totalElements());
    }
}