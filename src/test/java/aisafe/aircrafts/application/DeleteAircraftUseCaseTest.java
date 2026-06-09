package aisafe.aircrafts.application;

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
class DeleteAircraftUseCaseTest {

    @Mock
    private AircraftRepository aircraftRepository;

    @InjectMocks
    private DeleteAircraftUseCase deleteAircraftUseCase;

    private RegistrationNumber registrationNumber;
    private Aircraft aircraft;

    @BeforeEach
    void setUp() {
        registrationNumber = new RegistrationNumber("CS-TPA");
        AircraftModel model = new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "a320.jpg", 180);
        aircraft = new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1), model, registrationNumber, 150, 5000.0, List.of());
    }

    @Test
    void ensureAircraftIsDeletedSuccessfully() {
        when(aircraftRepository.findByRegistrationNumber(registrationNumber)).thenReturn(Optional.of(aircraft));

        assertDoesNotThrow(() -> deleteAircraftUseCase.execute(registrationNumber));
        verify(aircraftRepository, times(1)).delete(aircraft);
    }

    @Test
    void ensureExceptionWhenAircraftNotFound() {
        when(aircraftRepository.findByRegistrationNumber(registrationNumber)).thenReturn(Optional.empty());

        assertThrows(AircraftNotFoundException.class, () -> deleteAircraftUseCase.execute(registrationNumber));
        verify(aircraftRepository, never()).delete(any());
    }
}
