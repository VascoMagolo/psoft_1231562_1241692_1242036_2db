package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.ListAircraftsUseCaseResponse;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ListAircraftUseCase {

    private final AircraftRepository repository;

    public ListAircraftUseCase(AircraftRepository repository) {
        this.repository = repository;
    }

    public Page<ListAircraftsUseCaseResponse> execute(Pageable pageable) {
        Page<Aircraft> aircraftPage = repository.findAll(pageable);

        return aircraftPage.map(aircraft -> new ListAircraftsUseCaseResponse(
                aircraft.getRegistrationNumber().getNumber(),
                aircraft.getModel().getModelName(),
                aircraft.getStatus().name()
        ));
    }
}