package aisafe.aircrafts.application;

import aisafe.UseCase;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftInvalidFieldException;
import aisafe.aircrafts.domain.AircraftRepository;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@Transactional
public class RegisterAircraftUseCase {
    private final AircraftRepository repository;

    public RegisterAircraftUseCase(AircraftRepository repository) {
        this.repository = repository;
    }

    public Aircraft execute(Aircraft aircraft) {
        if (aircraft.getModel() == null) {
            throw new AircraftInvalidFieldException("Aircraft must have a model");
        }
        if (aircraft.getRegistrationNumber() == null) {
            throw new AircraftInvalidFieldException("Registration Number is invalid");
        }
        if (aircraft.getSeatCapacity() == null || aircraft.getSeatCapacity() <= 0) {
            // maybe later this verification will be better,
            // the aircraft model will have a min and max seating capacity
            // and a verification will be made based on that
            throw new AircraftInvalidFieldException("Seat capacity is invalid");
        }
        if (aircraft.getStatus() == null) {
            throw new AircraftInvalidFieldException("Aircraft must have a status");
        }
        return repository.save(aircraft);
    }
}