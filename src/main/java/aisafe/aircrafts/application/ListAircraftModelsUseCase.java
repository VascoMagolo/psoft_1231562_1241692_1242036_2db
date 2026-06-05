package aisafe.aircrafts.application;

import aisafe.shared.application.UseCase;
import aisafe.aircrafts.application.dtos.ListAircraftModelsUseCaseResponse;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
     * @param pageNumber the current page index
     * @param pageSize the number of items per page
     * @return a plain Java List of aircraft model DTOs
     */
    public List<ListAircraftModelsUseCaseResponse> execute(int pageNumber, int pageSize) {

        List<AircraftModel> modelsList = repository.findAll(pageNumber, pageSize);

        return modelsList.stream().map(model -> new ListAircraftModelsUseCaseResponse(
                model.getModelName(),
                model.getManufacturer().name(),
                model.getMaximumSeatingCapacity()
        )).collect(Collectors.toList());
    }
}