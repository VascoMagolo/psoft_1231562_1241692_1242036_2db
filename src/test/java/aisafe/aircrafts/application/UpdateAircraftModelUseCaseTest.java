package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.AircraftModelResponse;
import aisafe.aircrafts.application.dtos.UpdateAircraftModelRequest;
import aisafe.aircrafts.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateAircraftModelUseCaseTest {

    @Mock
    private AircraftModelRepository aircraftModelRepository;

    @InjectMocks
    private UpdateAircraftModelUseCase updateAircraftModelUseCase;

    private AircraftModel aircraftModel;

    @BeforeEach
    void setUp() {
        aircraftModel = new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "a320.jpg", 180);
    }

    @Test
    void ensureModelIsUpdatedSuccessfully() {
        UpdateAircraftModelRequest request = new UpdateAircraftModelRequest(850.0, 28000.0, 7000.0, 200, "a320_new.jpg");

        when(aircraftModelRepository.findByModelName("A320")).thenReturn(Optional.of(aircraftModel));

        AircraftModelResponse response = updateAircraftModelUseCase.execute("A320", request);

        assertNotNull(response);
        assertEquals(28000.0, response.fuelCapacity());
        assertEquals(200, response.maximumSeatingCapacity());
        verify(aircraftModelRepository, times(1)).save(aircraftModel);
    }

    @Test
    void ensureExceptionWhenModelNotFound() {
        UpdateAircraftModelRequest request = new UpdateAircraftModelRequest(850.0, 28000.0, 7000.0, 200, "a320_new.jpg");

        when(aircraftModelRepository.findByModelName("NON-EXISTENT")).thenReturn(Optional.empty());

        assertThrows(AircraftModelNotFoundException.class, () -> updateAircraftModelUseCase.execute("NON-EXISTENT", request));
        verify(aircraftModelRepository, never()).save(any());
    }
}
