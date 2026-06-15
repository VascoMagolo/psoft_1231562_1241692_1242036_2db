package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.UpdateAircraftRequest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateAircraftUseCaseTest {

    @Mock
    private AircraftRepository aircraftRepository;

    @Mock
    private AircraftModelRepository aircraftModelRepository;

    @InjectMocks
    private UpdateAircraftUseCase updateAircraftUseCase;

    private RegistrationNumber registrationNumber;
    private Aircraft aircraft;
    private AircraftModel model;

    @BeforeEach
    void setUp() {
        registrationNumber = new RegistrationNumber("CS-TPA");
        model = new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "a320.jpg", 180);
        aircraft = new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1), model, registrationNumber, 150, 5000.0, List.of());
    }

    @Test
    void ensureAircraftIsUpdatedSuccessfully() {
        UpdateAircraftRequest request = new UpdateAircraftRequest("A320", LocalDate.of(2021, 1, 1), 160, 5500.0, List.of("WiFi"), "INACTIVE");
        when(aircraftRepository.findByRegistrationNumber(registrationNumber)).thenReturn(Optional.of(aircraft));
        when(aircraftRepository.findVersionFor(registrationNumber)).thenReturn(0L).thenReturn(1L);
        when(aircraftModelRepository.findByModelName("A320")).thenReturn(Optional.of(model));

        ViewAircraftDetailsResponse response = updateAircraftUseCase.execute(registrationNumber, request, 0L);

        assertNotNull(response);
        assertEquals(AircraftStatus.INACTIVE, response.status());
        assertEquals(160, response.seatCapacity());
        verify(aircraftRepository, times(1)).save(aircraft);
    }

    @Test
    void ensureExceptionWhenAircraftNotFound() {
        UpdateAircraftRequest request = new UpdateAircraftRequest("A320", null, null, null, null, null);
        when(aircraftRepository.findByRegistrationNumber(registrationNumber)).thenReturn(Optional.empty());

        assertThrows(AircraftNotFoundException.class, () -> updateAircraftUseCase.execute(registrationNumber, request, 0L));
    }

    @Test
    void ensureExceptionWhenModelNotFound() {
        UpdateAircraftRequest request = new UpdateAircraftRequest("NON_EXISTENT", null, null, null, null, null);
        when(aircraftRepository.findByRegistrationNumber(registrationNumber)).thenReturn(Optional.of(aircraft));
        when(aircraftRepository.findVersionFor(registrationNumber)).thenReturn(0L);
        when(aircraftModelRepository.findByModelName("NON_EXISTENT")).thenReturn(Optional.empty());

        assertThrows(AircraftModelNotFoundException.class, () -> updateAircraftUseCase.execute(registrationNumber, request, 0L));
    }
}
