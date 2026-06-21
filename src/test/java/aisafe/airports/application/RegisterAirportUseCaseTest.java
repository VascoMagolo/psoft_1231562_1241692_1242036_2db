package aisafe.airports.application;

import aisafe.shared.domain.DuplicateResourceException;
import aisafe.airports.application.dtos.RegisterAirportRequest;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.IataCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterAirportUseCaseTest {

    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private RegisterAirportUseCase registerAirport;

    private RegisterAirportRequest buildRequest(String iataCode) {
        return new RegisterAirportRequest(
                iataCode, "Lisbon Airport", "Lisbon", "Portugal", "Europe", "Europe/Lisbon",
                38.77, -9.13,
                List.of(new RegisterAirportRequest.RunwayRequest("03/21", 3000, "030/210")),
                null, null, null, null, null, null);
    }

    @Test
    void ensureAirportIsRegisteredSuccessfully() {
        when(airportRepository.existsByIataCode(new IataCode("LIS"))).thenReturn(false);

        assertDoesNotThrow(() -> registerAirport.execute(buildRequest("LIS")));
        verify(airportRepository).save(any(Airport.class));
    }

    @Test
    void ensureExceptionWhenIataCodeAlreadyExists() {
        when(airportRepository.existsByIataCode(new IataCode("LIS"))).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> registerAirport.execute(buildRequest("LIS")));
        verify(airportRepository, never()).save(any());
    }

    @Test
    void ensureAirportIsRegisteredWithAllDetails() {
        when(airportRepository.existsByIataCode(new IataCode("LIS"))).thenReturn(false);

        RegisterAirportRequest request = new RegisterAirportRequest(
                "LIS", "Lisbon Airport", "Lisbon", "Portugal", "Europe", "Europe/Lisbon",
                38.77, -9.13,
                List.of(new RegisterAirportRequest.RunwayRequest("03/21", 3000, "030/210")),
                new byte[]{1, 2, 3}, "image/jpeg", "24/7",
                List.of("WiFi"), List.of("T1"), List.of("G1")
        );

        assertDoesNotThrow(() -> registerAirport.execute(request));
        verify(airportRepository).save(any(Airport.class));
    }
}
