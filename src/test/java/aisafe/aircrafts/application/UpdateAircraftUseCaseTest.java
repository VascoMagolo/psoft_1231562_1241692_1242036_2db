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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateAircraftUseCaseTest {

    @Mock
    private AircraftRepository aircraftRepository;

    @Mock
    private AircraftModelRepository aircraftModelRepository;

    @InjectMocks
    private UpdateAircraftUseCase updateAircraftUseCase;

    private Aircraft aircraft;
    private RegistrationNumber registrationNumber;
    private AircraftModel model;

    @BeforeEach
    void setUp() {
        registrationNumber = new RegistrationNumber("CS-TPA");
        model = new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "a320.jpg", 180);
        aircraft = new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1), model, registrationNumber, 150, List.of("WiFi"));
    }

    @Test
    void ensureAircraftIsUpdatedSuccessfully() {
        UpdateAircraftRequest request = new UpdateAircraftRequest("A321", LocalDate.of(2021, 1, 1), 160, List.of("WiFi", "Bluetooth"));
        AircraftModel newModel = new AircraftModel("A321", Manufacturer.AIRBUS, 27000.0, 6500.0, 840.0, "a321.jpg", 200);

        when(aircraftRepository.findByRegistrationNumber(registrationNumber)).thenReturn(Optional.of(aircraft));
        when(aircraftModelRepository.findByModelName("A321")).thenReturn(Optional.of(newModel));

        ViewAircraftDetailsResponse response = updateAircraftUseCase.execute(registrationNumber, request, 0L);

        assertNotNull(response);
        assertEquals("A321", response.model());
        assertEquals(LocalDate.of(2021, 1, 1), response.manufacturingDate());
        assertEquals(160, response.seatCapacity());
        assertTrue(response.features().contains("Bluetooth"));
        verify(aircraftRepository, times(1)).save(aircraft, 0L);
    }

    @Test
    void ensureExceptionWhenAircraftNotFound() {
        UpdateAircraftRequest request = new UpdateAircraftRequest("A321", null, null, null);

        when(aircraftRepository.findByRegistrationNumber(registrationNumber)).thenReturn(Optional.empty());

        assertThrows(AircraftNotFoundException.class, () -> updateAircraftUseCase.execute(registrationNumber, request, 0L));
        verify(aircraftRepository, never()).save(any(), anyLong());
    }

    @Test
    void ensureExceptionWhenModelNotFound() {
        UpdateAircraftRequest request = new UpdateAircraftRequest("NON-EXISTENT", null, null, null);

        when(aircraftRepository.findByRegistrationNumber(registrationNumber)).thenReturn(Optional.of(aircraft));
        when(aircraftModelRepository.findByModelName("NON-EXISTENT")).thenReturn(Optional.empty());

        assertThrows(AircraftModelNotFoundException.class, () -> updateAircraftUseCase.execute(registrationNumber, request, 0L));
        verify(aircraftRepository, never()).save(any(), anyLong());
    }
}
