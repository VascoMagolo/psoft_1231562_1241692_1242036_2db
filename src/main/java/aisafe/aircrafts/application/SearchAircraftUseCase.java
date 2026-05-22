package aisafe.aircrafts.application;

import aisafe.UseCase;
import aisafe.aircrafts.application.dtos.SearchAircraftUseCaseResponse;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.AircraftStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@Transactional(readOnly = true)
public class SearchAircraftUseCase {

    private final AircraftRepository repository;

    public SearchAircraftUseCase(AircraftRepository repository) {
        this.repository = repository;
    }

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