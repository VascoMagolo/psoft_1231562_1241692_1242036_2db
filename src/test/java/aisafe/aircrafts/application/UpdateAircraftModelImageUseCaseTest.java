package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.AircraftModelResponse;
import aisafe.aircrafts.application.dtos.UpdateAircraftModelImageRequest;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelNotFoundException;
import aisafe.aircrafts.domain.AircraftModelRepository;
import aisafe.aircrafts.domain.Manufacturer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateAircraftModelImageUseCaseTest {

    @Mock
    private AircraftModelRepository repository;

    @InjectMocks
    private UpdateAircraftModelImageUseCase useCase;

    private AircraftModel model() {
        return new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, null, 180);
    }

    @Test
    void ensureImageIsUpdated() {
        byte[] bytes = new byte[]{1, 2, 3};
        UpdateAircraftModelImageRequest request = new UpdateAircraftModelImageRequest("A320", bytes, "image/jpeg");

        when(repository.findByModelName("A320")).thenReturn(Optional.of(model()));

        AircraftModelResponse response = useCase.execute(request);

        assertNotNull(response);
        assertTrue(response.hasImage());
        verify(repository, times(1)).save(any(AircraftModel.class));
    }

    @Test
    void ensureThrowsWhenModelNotFound() {
        UpdateAircraftModelImageRequest request = new UpdateAircraftModelImageRequest("UNKNOWN", new byte[]{1}, "image/jpeg");

        when(repository.findByModelName("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(AircraftModelNotFoundException.class, () -> useCase.execute(request));
        verify(repository, never()).save(any());
    }
}
