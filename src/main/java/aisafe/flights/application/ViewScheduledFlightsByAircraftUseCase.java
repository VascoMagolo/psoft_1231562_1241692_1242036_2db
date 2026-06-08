package aisafe.flights.application;

import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.flights.domain.Flight;
import aisafe.flights.domain.FlightRepository;
import aisafe.shared.application.UseCase;
import lombok.RequiredArgsConstructor;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class ViewScheduledFlightsByAircraftUseCase {

    private final FlightRepository flightRepository;
    private final AircraftRepository aircraftRepository;

    public List<Flight> execute(String aircraftId) {
        String normalizedAircraftId = aircraftId.trim().toUpperCase();
        RegistrationNumber registrationNumber = new RegistrationNumber(normalizedAircraftId);
        if (!aircraftRepository.existsByRegistrationNumber(registrationNumber)) {
            throw new AircraftNotFoundException("Aircraft not found: " + normalizedAircraftId);
        }
        return flightRepository.findByAircraftRegistrationNumber(normalizedAircraftId);
    }
}
