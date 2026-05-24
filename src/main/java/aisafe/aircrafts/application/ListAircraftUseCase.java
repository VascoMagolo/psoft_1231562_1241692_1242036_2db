package aisafe.aircrafts.application;

import aisafe.shared.application.UseCase;
import aisafe.aircrafts.application.dtos.ListAircraftsUseCaseResponse;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

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
     * @param pageable pagination and sorting information
     * @return a page of aircraft DTOs
     */
    public Page<ListAircraftsUseCaseResponse> execute(Pageable pageable) {
        Page<Aircraft> aircraftPage = repository.findAll(pageable);

        return aircraftPage.map(aircraft -> new ListAircraftsUseCaseResponse(
                aircraft.getRegistrationNumber().getNumber(),
                aircraft.getModel().getModelName(),
                aircraft.getStatus().name()
        ));
    }
}