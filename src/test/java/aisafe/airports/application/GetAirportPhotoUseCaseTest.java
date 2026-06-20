package aisafe.airports.application;

import aisafe.shared.application.dtos.ImageData;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportPhoto;
import aisafe.airports.domain.AirportPhotoNotFoundException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAirportPhotoUseCaseTest {

    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private GetAirportPhotoUseCase getAirportPhoto;

    private Airport buildAirportWithPhotos() {
        Airport airport = new Airport("LIS", "Lisbon Airport", "Lisbon", "Portugal", "Europe",
                "Europe/Lisbon", 38.77, -9.13,
                List.of(new Runway("03/21", 3000, "030/210")));
        airport.addPhoto(new AirportPhoto(new byte[]{1, 2, 3}, "image/jpeg"));
        airport.addPhoto(new AirportPhoto(new byte[]{4, 5, 6}, "image/png"));
        return airport;
    }

    private Airport buildAirportWithoutPhotos() {
        return new Airport("LIS", "Lisbon Airport", "Lisbon", "Portugal", "Europe",
                "Europe/Lisbon", 38.77, -9.13,
                List.of(new Runway("03/21", 3000, "030/210")));
    }

    @Test
    void ensureFirstPhotoIsReturnedByIndex() {
        when(airportRepository.findByIataCode(new IataCode("LIS")))
                .thenReturn(Optional.of(buildAirportWithPhotos()));

        ImageData data = getAirportPhoto.execute("LIS", 0);

        assertArrayEquals(new byte[]{1, 2, 3}, data.bytes());
        assertEquals("image/jpeg", data.contentType());
    }

    @Test
    void ensureSecondPhotoIsReturnedByIndex() {
        when(airportRepository.findByIataCode(new IataCode("LIS")))
                .thenReturn(Optional.of(buildAirportWithPhotos()));

        ImageData data = getAirportPhoto.execute("LIS", 1);

        assertArrayEquals(new byte[]{4, 5, 6}, data.bytes());
        assertEquals("image/png", data.contentType());
    }

    @Test
    void ensureExceptionWhenAirportNotFound() {
        when(airportRepository.findByIataCode(new IataCode("XXX"))).thenReturn(Optional.empty());

        assertThrows(AirportNotFoundException.class, () -> getAirportPhoto.execute("XXX", 0));
    }

    @Test
    void ensureExceptionWhenAirportHasNoPhotos() {
        when(airportRepository.findByIataCode(new IataCode("LIS")))
                .thenReturn(Optional.of(buildAirportWithoutPhotos()));

        assertThrows(AirportPhotoNotFoundException.class, () -> getAirportPhoto.execute("LIS", 0));
    }

    @Test
    void ensureExceptionWhenIndexOutOfBounds() {
        when(airportRepository.findByIataCode(new IataCode("LIS")))
                .thenReturn(Optional.of(buildAirportWithPhotos()));

        assertThrows(AirportPhotoNotFoundException.class, () -> getAirportPhoto.execute("LIS", 99));
    }
}
