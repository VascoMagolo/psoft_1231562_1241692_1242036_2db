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
                "CS-TPA", 1L, LocalDate.of(2020, 1, 1), 150, "AVAILABLE", List.of("WiFi"));

        AircraftModel model = buildModel(180);
        when(modelRepository.findById(1L)).thenReturn(Optional.of(model));
        when(aircraftRepository.existsByRegistrationNumber(any(RegistrationNumber.class))).thenReturn(false);
        when(aircraftRepository.save(any(Aircraft.class))).thenAnswer(i -> i.getArguments()[0]);

        assertDoesNotThrow(() -> registerAircraft.execute(request));
        verify(aircraftRepository, times(1)).save(any(Aircraft.class));
    }

    @Test
    void ensureExceptionWhenModelNotFound() {
        RegisterAircraftRequest request = new RegisterAircraftRequest(
                "CS-TPA", 99L, LocalDate.of(2020, 1, 1), 150, "AVAILABLE", List.of());

        when(modelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> registerAircraft.execute(request));
        verify(aircraftRepository, never()).save(any());
    }

    @Test
    void ensureExceptionWhenRegistrationAlreadyExists() {
        RegisterAircraftRequest request = new RegisterAircraftRequest(
                "CS-TPA", 1L, LocalDate.of(2020, 1, 1), 150, "AVAILABLE", List.of());

        AircraftModel model = buildModel(180);
        when(modelRepository.findById(1L)).thenReturn(Optional.of(model));
        when(aircraftRepository.existsByRegistrationNumber(any(RegistrationNumber.class))).thenReturn(true);

        assertThrows(AircraftAlreadyExistsException.class, () -> registerAircraft.execute(request));
        verify(aircraftRepository, never()).save(any());
    }

    @Test
    void ensureExceptionWhenSeatCapacityExceedsModelMax() {
        RegisterAircraftRequest request = new RegisterAircraftRequest(
                "CS-TPA", 1L, LocalDate.of(2020, 1, 1), 200, "AVAILABLE", List.of());

        AircraftModel model = buildModel(150);
        when(modelRepository.findById(1L)).thenReturn(Optional.of(model));
        when(aircraftRepository.existsByRegistrationNumber(any(RegistrationNumber.class))).thenReturn(false);

        assertThrows(AircraftInvalidFieldException.class, () -> registerAircraft.execute(request));
        verify(aircraftRepository, never()).save(any());
    }
}
