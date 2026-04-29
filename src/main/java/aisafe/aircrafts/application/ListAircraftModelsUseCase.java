package aisafe.aircrafts.application;

import aisafe.UseCase;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@UseCase
@Transactional(readOnly = true)
public class ListAircraftModelsUseCase {

    private final AircraftModelRepository repository;

    public ListAircraftModelsUseCase(AircraftModelRepository repository) {
        this.repository = repository;
    }

    public List<AircraftModel> execute() {
        return repository.findAll();
    }
}