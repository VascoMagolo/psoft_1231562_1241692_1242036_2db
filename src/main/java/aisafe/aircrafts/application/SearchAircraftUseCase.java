package aisafe.aircrafts.application;

import aisafe.shared.application.UseCase;
import aisafe.aircrafts.application.dtos.SearchAircraftUseCaseResponse;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.AircraftStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
     * Search for aircrafts based on pure domain criteria.
     * ZERO Spring Data Pageable imports!
     */
    public List<SearchAircraftUseCaseResponse> execute(String modelName, String statusStr, Integer year, int pageNumber, int pageSize) {

        AircraftStatus status = statusStr != null ? AircraftStatus.valueOf(statusStr.toUpperCase()) : null;

        List<Aircraft> results = repository.searchAircrafts(modelName, status, year, pageNumber, pageSize);

        return results.stream().map(aircraft -> new SearchAircraftUseCaseResponse(
                aircraft.getRegistrationNumber().getNumber(),
                aircraft.getModel().getModelName(),
                aircraft.getStatus().name(),
                aircraft.getManufacturingDate().getYear()
        )).collect(Collectors.toList());
    }
}