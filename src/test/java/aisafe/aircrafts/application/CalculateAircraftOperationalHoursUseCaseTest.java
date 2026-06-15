package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.AircraftOperationalHoursResponse;
import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.routes.infrastructure.persistence.jpa.SpringDataScheduledFlightRepository;
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
    private SpringDataScheduledFlightRepository flightRepository;

    @Mock
    private AircraftRepository aircraftRepository;

    @InjectMocks
    private CalculateAircraftOperationalHoursUseCase useCase;

    @Test
    void executeReturnsCalculatedHours() {
        RegistrationNumber registration = new RegistrationNumber("CS-TKA");
        when(aircraftRepository.existsByRegistrationNumber(registration)).thenReturn(true);
        when(flightRepository.calculateTotalOperationalHoursByRegistration(registration.getNumber())).thenReturn(15.5);

        AircraftOperationalHoursResponse response = useCase.execute(registration);

        assertNotNull(response);
        assertEquals("CS-TKA", response.registrationNumber());
        assertEquals(15.5, response.totalOperationalHours());
    }

    @Test
    void executeReturnsZeroWhenNullHours() {
        RegistrationNumber registration = new RegistrationNumber("CS-TKA");
        when(aircraftRepository.existsByRegistrationNumber(registration)).thenReturn(true);
        when(flightRepository.calculateTotalOperationalHoursByRegistration(registration.getNumber())).thenReturn(null);

        AircraftOperationalHoursResponse response = useCase.execute(registration);

        assertNotNull(response);
        assertEquals("CS-TKA", response.registrationNumber());
        assertEquals(0.0, response.totalOperationalHours());
    }

    @Test
    void executeThrowsExceptionWhenAircraftNotFound() {
        RegistrationNumber registration = new RegistrationNumber("CS-XXX");
        when(aircraftRepository.existsByRegistrationNumber(registration)).thenReturn(false);

        assertThrows(AircraftNotFoundException.class, () -> useCase.execute(registration));
        verify(flightRepository, never()).calculateTotalOperationalHoursByRegistration(anyString());
    }
}
