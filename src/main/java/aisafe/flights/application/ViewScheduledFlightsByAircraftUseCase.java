package aisafe.flights.application;

import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.flights.application.dtos.FlightResponse;
import aisafe.flights.domain.ScheduledFlight;
import aisafe.flights.domain.ScheduledFlightRepository;
import aisafe.shared.application.UseCase;
import lombok.RequiredArgsConstructor;

import java.util.List;

@UseCase(readOnly = true)
@RequiredArgsConstructor
public class ViewScheduledFlightsByAircraftUseCase {

    private final ScheduledFlightRepository scheduledFlightRepository;
    private final AircraftRepository aircraftRepository;

    public List<FlightResponse> execute(String aircraftId) {
        String normalizedAircraftId = aircraftId.trim().toUpperCase();
        RegistrationNumber registrationNumber = new RegistrationNumber(normalizedAircraftId);
        if (!aircraftRepository.existsByRegistrationNumber(registrationNumber)) {
            throw new AircraftNotFoundException("Aircraft not found: " + normalizedAircraftId);
        }
        return scheduledFlightRepository.findByAircraftRegistration(normalizedAircraftId).stream()
                .map(FlightResponse::from)
                .toList();
    }
}
