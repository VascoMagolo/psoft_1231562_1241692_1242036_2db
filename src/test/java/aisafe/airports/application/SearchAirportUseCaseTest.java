package aisafe.airports.application;

import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.Runway;
import aisafe.shared.domain.PaginatedResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchAirportUseCaseTest {

    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private SearchAirportUseCase searchAirport;

    private Airport buildAirport(String iataCode) {
        return new Airport(iataCode, "Lisbon Airport", "Lisbon", "Portugal", "Europe",
                "Europe/Lisbon", 38.77, -9.13,
                List.of(new Runway("03/21", 3000, "030/210")));
    }

    @Test
    void ensureSearchReturnsPaginatedResults() {
        Airport airport = buildAirport("LIS");
        PaginatedResult<Airport> domainResult = new PaginatedResult<>(List.of(airport), 1L);
        when(airportRepository.searchAirports("Lisbon", null, null, 0, 10)).thenReturn(domainResult);

        PaginatedResult<AirportResponse> result = searchAirport.execute("Lisbon", null, null, 0, 10);

        assertEquals(1, result.data().size());
        assertEquals(1L, result.totalElements());
    }

    @Test
    void ensureSearchWithNoMatchReturnsEmptyResult() {
        PaginatedResult<Airport> domainResult = new PaginatedResult<>(List.of(), 0L);
        when(airportRepository.searchAirports("Unknown", null, null, 0, 10)).thenReturn(domainResult);

        PaginatedResult<AirportResponse> result = searchAirport.execute("Unknown", null, null, 0, 10);

        assertTrue(result.data().isEmpty());
        assertEquals(0L, result.totalElements());
    }
}
