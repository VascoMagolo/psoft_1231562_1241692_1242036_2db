package aisafe.aircrafts.application;

import aisafe.UseCase;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@UseCase
@Transactional(readOnly = true)
public class ListAircraftUseCase {
    private final AircraftRepository repository;

    public ListAircraftUseCase(AircraftRepository repository) {
        this.repository = repository;
    }

    public List<Aircraft> execute() {
        return repository.findAll();
    }
}