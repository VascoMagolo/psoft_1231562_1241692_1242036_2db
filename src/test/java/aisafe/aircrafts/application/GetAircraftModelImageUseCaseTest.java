package aisafe.aircrafts.application;

import aisafe.shared.application.dtos.ImageData;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelImage;
import aisafe.aircrafts.domain.AircraftModelImageNotFoundException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAircraftModelImageUseCaseTest {

    @Mock
    private AircraftModelRepository repository;

    @InjectMocks
    private GetAircraftModelImageUseCase useCase;

    @Test
    void ensureImageIsReturned() {
        byte[] bytes = new byte[]{1, 2, 3};
        AircraftModelImage image = new AircraftModelImage(bytes, "image/jpeg");
        AircraftModel model = new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, image, 180);

        when(repository.findByModelName("A320")).thenReturn(Optional.of(model));

        ImageData data = useCase.execute("A320");

        assertNotNull(data);
        assertArrayEquals(bytes, data.bytes());
        assertEquals("image/jpeg", data.contentType());
    }

    @Test
    void ensureThrowsWhenModelNotFound() {
        when(repository.findByModelName("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(AircraftModelNotFoundException.class, () -> useCase.execute("UNKNOWN"));
    }

    @Test
    void ensureThrowsWhenNoImage() {
        AircraftModel model = new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, null, 180);

        when(repository.findByModelName("A320")).thenReturn(Optional.of(model));

        assertThrows(AircraftModelImageNotFoundException.class, () -> useCase.execute("A320"));
    }
}
