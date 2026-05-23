package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.AircraftModelResponse;
import aisafe.aircrafts.application.dtos.RegisterAircraftModelRequest;
import aisafe.aircrafts.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterAircraftModelUseCaseTest {

    @Mock
    private AircraftModelRepository repository;

    @InjectMocks
    private RegisterAircraftModelUseCase registerAircraftModel;

    private RegisterAircraftModelRequest buildRequest() {
        return new RegisterAircraftModelRequest("A320", Manufacturer.AIRBUS, 6150.0, 26730.0, 833.0, 180, "a320.jpg");
    }

    @Test
    void ensureModelIsRegisteredSuccessfully() {
        when(repository.existsByModelName("A320")).thenReturn(false);
        when(repository.save(any(AircraftModel.class))).thenAnswer(i -> i.getArguments()[0]);

        AircraftModelResponse response = registerAircraftModel.execute(buildRequest());

        assertNotNull(response);
        assertEquals("A320", response.modelName());
        assertEquals(Manufacturer.AIRBUS, response.manufacturer());
        verify(repository, times(1)).save(any(AircraftModel.class));
    }

    @Test
    void ensureExceptionWhenModelNameAlreadyExists() {
        when(repository.existsByModelName("A320")).thenReturn(true);

        assertThrows(AircraftModelAlreadyExistsException.class, () ->
                registerAircraftModel.execute(buildRequest()));
        verify(repository, never()).save(any());
    }
}
