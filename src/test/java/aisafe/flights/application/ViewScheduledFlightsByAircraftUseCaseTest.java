package aisafe.flights.application;

import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.airports.domain.IataCode;
import aisafe.flights.application.dtos.FlightResponse;
import aisafe.flights.domain.ScheduledFlight;
import aisafe.flights.domain.ScheduledFlightRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViewScheduledFlightsByAircraftUseCaseTest {

    @Mock
    private ScheduledFlightRepository scheduledFlightRepository;
    @Mock
    private AircraftRepository aircraftRepository;

    @InjectMocks
    private ViewScheduledFlightsByAircraftUseCase useCase;

    @Test
    void ensureReturnsFlights() {
        String aircraftId = "CS-TPA";
        RegistrationNumber registrationNumber = new RegistrationNumber(aircraftId);
        when(aircraftRepository.existsByRegistrationNumber(any(RegistrationNumber.class))).thenReturn(true);
        
        ScheduledFlight flight = mock(ScheduledFlight.class);
        
        when(scheduledFlightRepository.findByAircraftRegistration(registrationNumber)).thenReturn(List.of(flight));
        
        // Mocking FlightResponse requirements
        when(flight.getAircraftRegistrationNumber()).thenReturn(new RegistrationNumber(aircraftId));
        when(flight.getOriginCode()).thenReturn(new IataCode("OPO"));
        when(flight.getDestinationCode()).thenReturn(new IataCode("LIS"));

        List<FlightResponse> result = useCase.execute(aircraftId);

        assertEquals(1, result.size());
        assertEquals(aircraftId, result.get(0).aircraftId());
    }

    @Test
    void ensureExceptionThrownWhenAircraftNotFound() {
        String aircraftId = "CS-TPA";
        when(aircraftRepository.existsByRegistrationNumber(any(RegistrationNumber.class))).thenReturn(false);

        assertThrows(AircraftNotFoundException.class, () -> useCase.execute(aircraftId));
        verify(scheduledFlightRepository, never()).findByAircraftRegistration(any());
    }
}
