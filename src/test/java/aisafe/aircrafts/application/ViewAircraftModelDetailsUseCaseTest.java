package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.AircraftModelResponse;
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
class ViewAircraftModelDetailsUseCaseTest {

    @Mock
    private AircraftModelRepository aircraftModelRepository;

    @InjectMocks
    private ViewAircraftModelDetailsUseCase viewAircraftModelDetailsUseCase;

    private AircraftModel aircraftModel;

    @BeforeEach
    void setUp() {
        aircraftModel = new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "a320.jpg", 180);
    }

    @Test
    void ensureModelDetailsAreReturnedSuccessfully() {
        when(aircraftModelRepository.findByModelName("A320")).thenReturn(Optional.of(aircraftModel));

        AircraftModelResponse response = viewAircraftModelDetailsUseCase.execute("A320");

        assertNotNull(response);
        assertEquals("A320", response.modelName());
        assertEquals(Manufacturer.AIRBUS, response.manufacturer());
    }

    @Test
    void ensureExceptionWhenModelNotFound() {
        when(aircraftModelRepository.findByModelName("NON-EXISTENT")).thenReturn(Optional.empty());

        assertThrows(AircraftModelNotFoundException.class, () -> viewAircraftModelDetailsUseCase.execute("NON-EXISTENT"));
    }
}
