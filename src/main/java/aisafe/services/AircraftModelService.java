package aisafe.services;


import aisafe.DomainException;
import aisafe.model.entities.AircraftModel;
import aisafe.repositories.AircraftModelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AircraftModelService {

    private final AircraftModelRepository repository;

    public AircraftModelService(AircraftModelRepository repository) {
        this.repository = repository;
    }

    public AircraftModel registerAircraftModel(AircraftModel newModel){
        if (newModel.getMaxRange() == null || newModel.getMaxRange() <= 0){
            throw new DomainException("Max Range is invalid");
        } // need to add more validations later

        return repository.save(newModel);
    }

    public List<AircraftModel> findAll() {
        return repository.findAll();
    }
}
