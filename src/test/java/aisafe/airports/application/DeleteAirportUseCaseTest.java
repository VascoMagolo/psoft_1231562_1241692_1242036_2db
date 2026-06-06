package aisafe.airports.application;

import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
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
class DeleteAirportUseCaseTest {

    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private DeleteAirportUseCase deleteAirport;

    private Airport buildAirport(String iataCode) {
        return new Airport(iataCode, "Lisbon Airport", "Lisbon", "Portugal", "Europe",
                "Europe/Lisbon", 38.77, -9.13,
                List.of(new Runway("03/21", 3000, "030/210")));
    }

    @Test
    void ensureAirportIsDeletedSuccessfully() {
        Airport airport = buildAirport("LIS");
        when(airportRepository.findByIataCodeCode("LIS")).thenReturn(Optional.of(airport));

        assertDoesNotThrow(() -> deleteAirport.execute("LIS"));
        verify(airportRepository).delete(any(Airport.class));
    }

    @Test
    void ensureExceptionWhenAirportNotFound() {
        when(airportRepository.findByIataCodeCode("XXX")).thenReturn(Optional.empty());

        assertThrows(AirportNotFoundException.class, () -> deleteAirport.execute("XXX"));
        verify(airportRepository, never()).delete(any());
    }
}
