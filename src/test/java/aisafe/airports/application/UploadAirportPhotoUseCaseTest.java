package aisafe.airports.application;

import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.IataCode;
import aisafe.airports.domain.Runway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadAirportPhotoUseCaseTest {

    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private UploadAirportPhotoUseCase uploadAirportPhoto;

    private Airport buildAirport() {
        return new Airport("LIS", "Lisbon Airport", "Lisbon", "Portugal", "Europe",
                "Europe/Lisbon", 38.77, -9.13,
                List.of(new Runway("03/21", 3000, "030/210")));
    }

    @Test
    void ensurePhotoIsAppendedSuccessfully() {
        IataCode code = new IataCode("LIS");
        Airport airport = buildAirport();
        when(airportRepository.findByIataCode(code)).thenReturn(Optional.of(airport));
        when(airportRepository.findVersionFor(code)).thenReturn(0L);

        uploadAirportPhoto.execute("LIS", new byte[]{1, 2, 3}, "image/jpeg");

        assertEquals(1, airport.getPhotos().size());
        verify(airportRepository).save(airport);
    }

    @Test
    void ensureMultiplePhotosAreAppended() {
        IataCode code = new IataCode("LIS");
        Airport airport = buildAirport();
        when(airportRepository.findByIataCode(code)).thenReturn(Optional.of(airport));
        when(airportRepository.findVersionFor(code)).thenReturn(0L);

        uploadAirportPhoto.execute("LIS", new byte[]{1}, "image/jpeg");
        uploadAirportPhoto.execute("LIS", new byte[]{2}, "image/png");

        assertEquals(2, airport.getPhotos().size());
    }

    @Test
    void ensureExceptionWhenAirportNotFound() {
        when(airportRepository.findByIataCode(new IataCode("XXX"))).thenReturn(Optional.empty());

        assertThrows(AirportNotFoundException.class,
                () -> uploadAirportPhoto.execute("XXX", new byte[]{1}, "image/jpeg"));
        verify(airportRepository, never()).save(any());
    }
}
