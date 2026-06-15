package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.AircraftOperationalHoursResponse;
import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.flights.domain.ScheduledFlightRepository;
import aisafe.shared.application.UseCase;

/**
 * Calculates the total operational hours for a specific aircraft in the fleet.
 */
@UseCase(readOnly = true)
public class CalculateAircraftOperationalHoursUseCase {

    private final ScheduledFlightRepository flightRepository;
    private final AircraftRepository aircraftRepository;

    public CalculateAircraftOperationalHoursUseCase(ScheduledFlightRepository flightRepository, 
                                                    AircraftRepository aircraftRepository) {
        this.flightRepository = flightRepository;
        this.aircraftRepository = aircraftRepository;
    }

    public AircraftOperationalHoursResponse execute(RegistrationNumber registrationNumber) {
        if (!aircraftRepository.existsByRegistrationNumber(registrationNumber)) {
            throw new AircraftNotFoundException("Aircraft with registration " + registrationNumber.getNumber() + " not found.");
        }

        Double totalHours = flightRepository.calculateTotalOperationalHoursByRegistration(registrationNumber);
        if (totalHours == null) {
            totalHours = 0.0;
        }

        return new AircraftOperationalHoursResponse(registrationNumber.getNumber(), totalHours);
    }
}
