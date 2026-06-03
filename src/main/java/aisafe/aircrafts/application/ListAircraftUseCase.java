package aisafe.aircrafts.application;

import aisafe.shared.application.UseCase;
import aisafe.aircrafts.application.dtos.ListAircraftsUseCaseResponse;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Returns all stored aircrafts for the aircraft management screens and APIs.
 * This use case is read-only and supports pagination and sorting.
 * The returned DTOs are lightweight and only contain fields needed for listing, not full details.
 */
@UseCase
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
    public List<ListAircraftsUseCaseResponse> execute(int pageNumber, int pageSize) {

        List<Aircraft> aircraftList = repository.findAll(pageNumber, pageSize);

        return aircraftList.stream().map(aircraft -> new ListAircraftsUseCaseResponse(
                aircraft.getRegistrationNumber().getNumber(),
                aircraft.getModel().getModelName(),
                aircraft.getStatus().name()
        )).collect(Collectors.toList());
    }
}