package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.RegisterAircraftRequest;
import aisafe.aircrafts.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterAircraftUseCaseTest {

    @Mock
    private AircraftRepository aircraftRepository;

    @Mock
    private AircraftModelRepository modelRepository;

    @InjectMocks
    private RegisterAircraftUseCase registerAircraft;

    private AircraftModel buildModel(int maxSeats) {
        return new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "a320.jpg", maxSeats);
    }

    @Test
    void ensureAircraftIsRegisteredSuccessfully() {
        RegisterAircraftRequest request = new RegisterAircraftRequest(
                "CS-TPA", "A320", LocalDate.of(2020, 1, 1), 150, 5000.0, "AVAILABLE", List.of("WiFi"));

        AircraftModel model = buildModel(180);
        when(modelRepository.findByModelName("A320")).thenReturn(Optional.of(model));
        when(aircraftRepository.existsByRegistrationNumber(any(RegistrationNumber.class))).thenReturn(false);

        assertDoesNotThrow(() -> registerAircraft.execute(request));
        verify(aircraftRepository, times(1)).save(any(Aircraft.class), any());
    }

    @Test
    void ensureExceptionWhenModelNotFound() {
        RegisterAircraftRequest request = new RegisterAircraftRequest(
                "CS-TPA", "NON-EXISTENT", LocalDate.of(2020, 1, 1), 150, 5000.0, "AVAILABLE", List.of());

        when(modelRepository.findByModelName("NON-EXISTENT")).thenReturn(Optional.empty());

        assertThrows(AircraftInvalidFieldException.class, () -> registerAircraft.execute(request));
        verify(aircraftRepository, never()).save(any(), any());
    }

    @Test
    void ensureExceptionWhenRegistrationAlreadyExists() {
        RegisterAircraftRequest request = new RegisterAircraftRequest(
                "CS-TPA", "A320", LocalDate.of(2020, 1, 1), 150, 5000.0, "AVAILABLE", List.of());

        AircraftModel model = buildModel(180);
        when(modelRepository.findByModelName("A320")).thenReturn(Optional.of(model));
        when(aircraftRepository.existsByRegistrationNumber(any(RegistrationNumber.class))).thenReturn(true);

        assertThrows(AircraftAlreadyExistsException.class, () -> registerAircraft.execute(request));
        verify(aircraftRepository, never()).save(any(), any());
    }

    @Test
    void ensureExceptionWhenSeatCapacityExceedsModelMax() {
        RegisterAircraftRequest request = new RegisterAircraftRequest(
                "CS-TPA", "A320", LocalDate.of(2020, 1, 1), 200, 5000.0, "AVAILABLE", List.of());

        AircraftModel model = buildModel(150);
        when(modelRepository.findByModelName("A320")).thenReturn(Optional.of(model));
        when(aircraftRepository.existsByRegistrationNumber(any(RegistrationNumber.class))).thenReturn(false);

        assertThrows(AircraftInvalidFieldException.class, () -> registerAircraft.execute(request));
        verify(aircraftRepository, never()).save(any(), any());
    }
}
