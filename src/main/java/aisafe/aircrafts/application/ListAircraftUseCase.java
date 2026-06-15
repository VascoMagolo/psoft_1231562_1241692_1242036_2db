package aisafe.aircrafts.application;

import aisafe.shared.application.UseCase;
import aisafe.aircrafts.application.dtos.ListAircraftsUseCaseResponse;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.shared.domain.PaginatedResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Returns all stored aircrafts for the aircraft management screens and APIs.
 * This use case is read-only and supports pagination and sorting.
 * The returned DTOs are lightweight and only contain fields needed for listing, not full details.
 */
@UseCase(readOnly = true)
@Transactional(readOnly = true)
public class ListAircraftUseCase {

    private final AircraftRepository repository;

    public ListAircraftUseCase(AircraftRepository repository) {
        this.repository = repository;
    }

    /**
     * Return all aircrafts as lightweight DTOs used by the API/UI.
     * @param pageNumber the current page index (starting from 0)
     * @param pageSize the number of items per page
     * @return a pure Java List of aircraft DTOs
     */
    public PaginatedResult<ListAircraftsUseCaseResponse> execute(int pageNumber, int pageSize) {
        PaginatedResult<Aircraft> domainResult = repository.findAll(pageNumber, pageSize);

        List<ListAircraftsUseCaseResponse> dtoList = domainResult.data().stream()
                .map(a -> ListAircraftsUseCaseResponse.from(a, repository.findVersionFor(a.getRegistrationNumber())))
                .collect(Collectors.toList());

        return new PaginatedResult<>(dtoList, domainResult.totalElements());
    }
}