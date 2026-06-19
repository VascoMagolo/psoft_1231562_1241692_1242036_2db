package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.RegisterAircraftRequest;
import aisafe.aircrafts.application.dtos.ViewAircraftDetailsResponse;
import aisafe.aircrafts.domain.*;
import aisafe.shared.domain.DuplicateResourceException;
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

    private AircraftModel model;
    private RegisterAircraftRequest request;

    @BeforeEach
    void setUp() {
        model = new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, null, 180);
        request = new RegisterAircraftRequest("CS-TPA", "A320", LocalDate.of(2020, 1, 1), 150, 5000.0, "AVAILABLE", List.of());
    }

    @Test
    void ensureAircraftIsRegisteredSuccessfully() {
        when(modelRepository.findByModelName("A320")).thenReturn(Optional.of(model));
        when(aircraftRepository.existsByRegistrationNumber(any())).thenReturn(false);
        when(aircraftRepository.findVersionFor(any())).thenReturn(0L);

        ViewAircraftDetailsResponse response = registerAircraft.execute(request);

        assertNotNull(response);
        assertEquals("CS-TPA", response.registrationNumber());
        verify(aircraftRepository, times(1)).save(any(Aircraft.class));
    }

    @Test
    void ensureExceptionWhenModelNotFound() {
        when(modelRepository.findByModelName("A320")).thenReturn(Optional.empty());

        assertThrows(AircraftInvalidFieldException.class, () -> registerAircraft.execute(request));
        verify(aircraftRepository, never()).save(any());
    }

    @Test
    void ensureExceptionWhenRegistrationAlreadyExists() {
        when(modelRepository.findByModelName("A320")).thenReturn(Optional.of(model));
        when(aircraftRepository.existsByRegistrationNumber(any())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> registerAircraft.execute(request));
        verify(aircraftRepository, never()).save(any());
    }

    @Test
    void ensureExceptionWhenSeatCapacityExceedsModel() {
        request = new RegisterAircraftRequest("CS-TPA", "A320", LocalDate.of(2020, 1, 1), 200, 5000.0, "AVAILABLE", List.of());
        when(modelRepository.findByModelName("A320")).thenReturn(Optional.of(model));

        assertThrows(AircraftInvalidFieldException.class, () -> registerAircraft.execute(request));
    }
}
