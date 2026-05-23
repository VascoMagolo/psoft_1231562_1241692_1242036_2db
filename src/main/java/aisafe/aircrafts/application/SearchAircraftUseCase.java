package aisafe.aircrafts.application;

import aisafe.UseCase;
import aisafe.aircrafts.application.dtos.SearchAircraftUseCaseResponse;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.AircraftStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Searches for aircrafts based on various criteria such as model, status, and manufacturing year.
 * This use case is read-only and supports pagination and sorting.
 * The returned DTOs are lightweight and only contain fields needed for listing, not full details.
 */
@UseCase
@Transactional(readOnly = true)
public class SearchAircraftUseCase {

    private final AircraftRepository repository;

    public SearchAircraftUseCase(AircraftRepository repository) {
        this.repository = repository;
    }

    /**
     * Search for aircrafts based on the provided criteria.
     * @param modelId the ID of the aircraft model to filter by (optional)
     * @param status the status of the aircraft to filter by (optional)
     * @param year the manufacturing year to filter by (optional)
     * @param pageable pagination and sorting information
     * @return a page of aircraft DTOs matching the search criteria
     */
    public Page<SearchAircraftUseCaseResponse> execute(Long modelId, AircraftStatus status, Integer year, Pageable pageable) {

        Page<Aircraft> resultPage = repository.searchAircrafts(modelId, status, year, pageable);

        return resultPage.map(aircraft -> new SearchAircraftUseCaseResponse(
                aircraft.getRegistrationNumber().getNumber(),
                aircraft.getModel().getModelName(),
                aircraft.getStatus().name(),
                aircraft.getManufacturingDate().getYear()
        ));
    }
}