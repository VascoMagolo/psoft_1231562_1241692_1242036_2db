package aisafe.aircrafts.application;

import aisafe.aircrafts.domain.AircraftInvalidFieldException;
import aisafe.shared.application.UseCase;
import aisafe.aircrafts.application.dtos.SearchAircraftUseCaseResponse;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.AircraftStatus;
import aisafe.shared.domain.PaginatedResult;
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
    public PaginatedResult<SearchAircraftUseCaseResponse> execute(String modelName, String statusStr, Integer year, String feature, int pageNumber, int pageSize) {

        AircraftStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = AircraftStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new AircraftInvalidFieldException("Invalid aircraft status: " + statusStr);
            }
        }
        PaginatedResult<Aircraft> domainResult = repository.searchAircrafts(modelName, status, year, feature, pageNumber, pageSize);

        List<SearchAircraftUseCaseResponse> dtoList = domainResult.data().stream()
                .map(SearchAircraftUseCaseResponse::from)
                .collect(Collectors.toList());

        return new PaginatedResult<>(dtoList, domainResult.totalElements());
    }
}