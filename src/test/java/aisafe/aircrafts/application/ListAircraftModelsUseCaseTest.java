package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.ListAircraftModelsUseCaseResponse;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelRepository;
import aisafe.aircrafts.domain.Manufacturer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListAircraftModelsUseCaseTest {

    @Mock
    private AircraftModelRepository aircraftModelRepository;

    @InjectMocks
    private ListAircraftModelsUseCase listAircraftModelsUseCase;

    private AircraftModel aircraftModel;

    @BeforeEach
    void setUp() {
        aircraftModel = new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "a320.jpg", 180);
    }

    @Test
    void ensureListReturnsModelsSuccessfully() {
        when(aircraftModelRepository.findAll(0, 10)).thenReturn(List.of(aircraftModel));

        List<ListAircraftModelsUseCaseResponse> result = listAircraftModelsUseCase.execute(0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        
        ListAircraftModelsUseCaseResponse response = result.get(0);
        assertEquals("A320", response.modelName());
        assertEquals(Manufacturer.AIRBUS, response.manufacturer());
        assertEquals(26730.0, response.fuelCapacity());
        assertEquals(6150.0, response.maxRange());
        assertEquals(833.0, response.cruisingSpeed());
        assertEquals("a320.jpg", response.imagePath());
        assertEquals(180, response.maximumSeatingCapacity());
    }
}
