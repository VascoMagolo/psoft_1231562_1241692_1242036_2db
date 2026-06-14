package aisafe.airports.application;

import aisafe.airports.application.dtos.AirportStatisticsResponse;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.AirportStatisticsData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AirportStatisticsUseCaseTest {

    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private AirportStatisticsUseCase airportStatistics;

    @Test
    void ensureEmptyResultReturnsEmptyList() {
        when(airportRepository.findStatistics()).thenReturn(List.of());

        List<AirportStatisticsResponse> result = airportStatistics.execute();

        assertTrue(result.isEmpty());
    }

    @Test
    void ensureResultPreservesOrderFromRepository() {
        var data = List.of(
                new AirportStatisticsData("LIS", "Lisbon Airport", "Lisbon", "Portugal", 3L),
                new AirportStatisticsData("OPO", "Porto Airport", "Porto", "Portugal", 2L)
        );
        when(airportRepository.findStatistics()).thenReturn(data);

        List<AirportStatisticsResponse> result = airportStatistics.execute();

        assertEquals(2, result.size());
        assertEquals("LIS", result.get(0).iataCode());
        assertEquals(3L, result.get(0).routeCount());
        assertEquals("OPO", result.get(1).iataCode());
        assertEquals(2L, result.get(1).routeCount());
    }
}
