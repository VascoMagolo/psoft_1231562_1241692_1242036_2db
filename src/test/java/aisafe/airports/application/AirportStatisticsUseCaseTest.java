package aisafe.airports.application;

import aisafe.airports.application.dtos.AirportStatisticsResponse;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.IataCode;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
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

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private AirportStatisticsUseCase airportStatistics;

    @Test
    void ensureEmptyFleetReturnsEmptyList() {
        when(airportRepository.findAll()).thenReturn(List.of());
        when(routeRepository.findAll()).thenReturn(List.of());

        List<AirportStatisticsResponse> result = airportStatistics.execute();

        assertTrue(result.isEmpty());
    }

    @Test
    void ensureAirportsAreSortedByDescendingRouteCount() {
        Airport lis = mock(Airport.class);
        Airport opo = mock(Airport.class);
        IataCode lisCode = new IataCode("LIS");
        IataCode opoCode = new IataCode("OPO");

        when(lis.getId()).thenReturn(1L);
        when(lis.getIataCode()).thenReturn(lisCode);
        when(lis.getName()).thenReturn("Lisbon Airport");
        when(lis.getCity()).thenReturn("Lisbon");
        when(lis.getCountry()).thenReturn("Portugal");

        when(opo.getId()).thenReturn(2L);
        when(opo.getIataCode()).thenReturn(opoCode);
        when(opo.getName()).thenReturn("Porto Airport");
        when(opo.getCity()).thenReturn("Porto");
        when(opo.getCountry()).thenReturn("Portugal");

        Route lisToOpo = new Route("LIS", "OPO", 30, 150.0, 80);
        Route opoToLis = new Route("OPO", "LIS", 30, 150.0, 80);
        Route lisToMad = new Route("LIS", "MAD", 60, 300.0, 150);

        when(airportRepository.findAll()).thenReturn(List.of(lis, opo));
        when(routeRepository.findAll()).thenReturn(List.of(lisToOpo, opoToLis, lisToMad));

        List<AirportStatisticsResponse> result = airportStatistics.execute();

        assertEquals(2, result.size());
        assertEquals("LIS", result.get(0).iataCode());
        assertEquals(3L, result.get(0).routeCount());
        assertEquals("OPO", result.get(1).iataCode());
        assertEquals(2L, result.get(1).routeCount());
    }
}
