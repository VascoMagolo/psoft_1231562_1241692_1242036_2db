package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.CalculateAircraftOperationalHoursRequest;
import aisafe.aircrafts.application.dtos.AircraftOperationalHoursResponse;
import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.flights.domain.ScheduledFlightRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculateAircraftOperationalHoursUseCaseTest {

    @Mock
    private ScheduledFlightRepository flightRepository;

    @Mock
    private AircraftRepository aircraftRepository;

    @InjectMocks
    private CalculateAircraftOperationalHoursUseCase useCase;

    @Test
    void ensureReturnsCalculatedHours() {
        RegistrationNumber registration = new RegistrationNumber("CS-TKA");
        when(aircraftRepository.existsByRegistrationNumber(registration)).thenReturn(true);
        when(flightRepository.calculateTotalOperationalHoursByRegistration(registration)).thenReturn(15.5);

        AircraftOperationalHoursResponse response = useCase.execute(new CalculateAircraftOperationalHoursRequest(registration));

        assertNotNull(response);
        assertEquals("CS-TKA", response.registrationNumber());
        assertEquals(15.5, response.totalOperationalHours());
    }

    @Test
    void ensureReturnsZeroWhenNullHours() {
        RegistrationNumber registration = new RegistrationNumber("CS-TKA");
        when(aircraftRepository.existsByRegistrationNumber(registration)).thenReturn(true);
        when(flightRepository.calculateTotalOperationalHoursByRegistration(registration)).thenReturn(null);

        AircraftOperationalHoursResponse response = useCase.execute(new CalculateAircraftOperationalHoursRequest(registration));

        assertNotNull(response);
        assertEquals("CS-TKA", response.registrationNumber());
        assertEquals(0.0, response.totalOperationalHours());
    }

    @Test
    void ensureThrowsExceptionWhenAircraftNotFound() {
        RegistrationNumber registration = new RegistrationNumber("CS-XXX");
        when(aircraftRepository.existsByRegistrationNumber(registration)).thenReturn(false);

        assertThrows(AircraftNotFoundException.class, () -> useCase.execute(new CalculateAircraftOperationalHoursRequest(registration)));
        verify(flightRepository, never()).calculateTotalOperationalHoursByRegistration(any(RegistrationNumber.class));
    }
}
