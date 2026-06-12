package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.AircraftOperationalHoursResponse;
import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.routes.infrastructure.persistence.SpringDataScheduledFlightRepository;
import aisafe.shared.application.UseCase;
import org.springframework.transaction.annotation.Transactional;

/**
 * Calculates the total operational hours for a specific aircraft in the fleet.
 */
@UseCase
@Transactional(readOnly = true)
public class CalculateAircraftOperationalHoursUseCase {

    private final SpringDataScheduledFlightRepository flightRepository;
    private final AircraftRepository aircraftRepository;

    public CalculateAircraftOperationalHoursUseCase(SpringDataScheduledFlightRepository flightRepository, 
                                                    AircraftRepository aircraftRepository) {
        this.flightRepository = flightRepository;
        this.aircraftRepository = aircraftRepository;
    }

    public AircraftOperationalHoursResponse execute(RegistrationNumber registrationNumber) {
        if (!aircraftRepository.existsByRegistrationNumber(registrationNumber)) {
            throw new AircraftNotFoundException("Aircraft with registration " + registrationNumber.getNumber() + " not found.");
        }

        Double totalHours = flightRepository.calculateTotalOperationalHoursByRegistration(registrationNumber.getNumber());
        if (totalHours == null) {
            totalHours = 0.0;
        }

        return new AircraftOperationalHoursResponse(registrationNumber.getNumber(), totalHours);
    }
}
