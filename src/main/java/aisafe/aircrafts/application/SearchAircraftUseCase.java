package aisafe.aircrafts.application;


import aisafe.UseCase;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftStatus;
import aisafe.aircrafts.domain.AircraftRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@UseCase
@Transactional(readOnly = true)
public class SearchAircraftUseCase {
    private final AircraftRepository repository;

    public SearchAircraftUseCase(AircraftRepository repository) {
        this.repository = repository;
    }

    public List<Aircraft> execute(Long modelId, AircraftStatus status, Integer year) {
        return repository.searchAircrafts(modelId, status, year);
    }
}
