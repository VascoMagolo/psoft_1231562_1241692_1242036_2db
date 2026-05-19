package aisafe.aircrafts.application;


import aisafe.UseCase;
import aisafe.aircrafts.application.dtos.SearchAircraftUseCaseResponse;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftStatus;
import aisafe.aircrafts.domain.AircraftRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Searches aircraft by optional model, status, and manufacturing year filters.
 */
@UseCase
@Transactional(readOnly = true)
public class SearchAircraftUseCase {
    private final AircraftRepository repository;

    public SearchAircraftUseCase(AircraftRepository repository) {
        this.repository = repository;
    }

    public List<SearchAircraftUseCaseResponse> execute(Long modelId, AircraftStatus status, Integer year) {
        return repository.searchAircrafts(modelId, status, year).stream()
                .map(this::toResponse)
                .toList();
    }

    private SearchAircraftUseCaseResponse toResponse(Aircraft aircraft) {
        return new SearchAircraftUseCaseResponse(
                aircraft.getRegistrationNumber().getNumber(),
                aircraft.getModel().getModelName(),
                aircraft.getModel().getManufacturer(),
                aircraft.getManufacturingDate(),
                aircraft.getStatus(),
                aircraft.getSeatCapacity(),
                aircraft.getFeatures()
        );
    }
}
