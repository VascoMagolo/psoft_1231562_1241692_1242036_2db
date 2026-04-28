package aisafe.services;

import aisafe.DomainException;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AircraftService {
    private final AircraftRepository repository;

    public AircraftService(AircraftRepository repository) {
        this.repository = repository;
    }

    public Aircraft registerAircraft(Aircraft newAircraft){
        if (newAircraft.getModel()==null){
            throw new DomainException("Aircraft must have from a model");
            // add some verifictations
        }
        return repository.save(newAircraft);
    }
    public List<Aircraft> findAll() {
        return repository.findAll();
    }
}
