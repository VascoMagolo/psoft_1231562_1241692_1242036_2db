package aisafe.aircrafts.application;

import aisafe.shared.application.UseCase;
import aisafe.aircrafts.application.dtos.ListAircraftModelsUseCaseResponse;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

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
     * @param pageable pagination and sorting information
     * @return a page of aircraft model DTOs
     */
    public Page<ListAircraftModelsUseCaseResponse> execute(Pageable pageable) {

        Page<AircraftModel> modelsPage = repository.findAll(pageable);

        return modelsPage.map(model -> new ListAircraftModelsUseCaseResponse(
                model.getId(),
                model.getModelName(),
                model.getManufacturer().name(),
                model.getMaximumSeatingCapacity()
        ));
    }
}