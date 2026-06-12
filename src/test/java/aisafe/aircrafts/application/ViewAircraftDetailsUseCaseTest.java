package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.ViewAircraftDetailsResponse;
import aisafe.aircrafts.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViewAircraftDetailsUseCaseTest {

    @Mock
    private AircraftRepository aircraftRepository;

    @InjectMocks
    private ViewAircraftDetailsUseCase viewAircraftDetailsUseCase;

    private RegistrationNumber registrationNumber;
    private Aircraft aircraft;

    @BeforeEach
    void setUp() {
        registrationNumber = new RegistrationNumber("CS-TPA");
        AircraftModel model = new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "a320.jpg", 180);
        aircraft = new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1), model, registrationNumber, 150, 5000.0, List.of("WiFi"));
    }

    @Test
    void ensureAircraftDetailsAreReturnedSuccessfully() {
        when(aircraftRepository.findByRegistrationNumber(registrationNumber)).thenReturn(Optional.of(aircraft));

        ViewAircraftDetailsResponse response = viewAircraftDetailsUseCase.execute(registrationNumber);

        assertNotNull(response);
        assertEquals("CS-TPA", response.registrationNumber());
        assertEquals("A320", response.model());
        assertEquals(AircraftStatus.AVAILABLE, response.status());
    }

    @Test
    void ensureExceptionWhenAircraftNotFound() {
        when(aircraftRepository.findByRegistrationNumber(registrationNumber)).thenReturn(Optional.empty());

        assertThrows(AircraftNotFoundException.class, () -> viewAircraftDetailsUseCase.execute(registrationNumber));
    }
}
