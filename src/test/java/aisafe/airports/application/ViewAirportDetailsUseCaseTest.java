package aisafe.airports.application;

import aisafe.airports.application.dtos.AirportResponse;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViewAirportDetailsUseCaseTest {

    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private ViewAirportDetailsUseCase viewAirportDetails;

    private Airport buildAirport(String iataCode) {
        return new Airport(iataCode, "Lisbon Airport", "Lisbon", "Portugal", "Europe",
                "Europe/Lisbon", 38.77, -9.13,
                List.of(new Runway("03/21", 3000, "030/210")));
    }

    @Test
    void ensureAirportDetailsReturnedSuccessfully() {
        Airport airport = buildAirport("LIS");
        when(airportRepository.findByIataCodeCode("LIS")).thenReturn(Optional.of(airport));

        AirportResponse result = viewAirportDetails.execute("LIS");

        assertNotNull(result);
        verify(airportRepository).findByIataCodeCode("LIS");
    }

    @Test
    void ensureExceptionWhenAirportNotFound() {
        when(airportRepository.findByIataCodeCode("XXX")).thenReturn(Optional.empty());

        assertThrows(AirportNotFoundException.class, () -> viewAirportDetails.execute("XXX"));
    }
}
