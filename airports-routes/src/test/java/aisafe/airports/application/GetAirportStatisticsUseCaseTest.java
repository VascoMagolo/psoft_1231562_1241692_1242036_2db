package aisafe.airports.application;

import aisafe.airports.application.dtos.AirportStatisticsResponse;
import aisafe.airports.domain.Airport;
import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.domain.Runway;
import aisafe.routes.application.RoutesAcrossReplicas;
import aisafe.routes.domain.Route;
import aisafe.shared.application.Versioned;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAirportStatisticsUseCaseTest {

    @Mock
    private AirportListings airports;

    @Mock
    private RoutesAcrossReplicas routes;

    @InjectMocks
    private GetAirportStatisticsUseCase airportStatistics;

    private static AirportResponse airport(String code, String city) {
        return AirportResponse.from(new Airport(code, city + " Airport", city, "Portugal", "Europe", "Europe/Lisbon",
                38.77, -9.13, List.of(new Runway("03/21", 3000, "030/210"))));
    }

    private static Versioned<Route> route(String origin, String destination) {
        return new Versioned<>(new Route(origin, destination, 45, 300.0, 100), 0L);
    }

    @Test
    void ensureEmptyResultReturnsEmptyList() {
        when(airports.findAll()).thenReturn(List.of());
        when(routes.findAll()).thenReturn(List.of());

        List<AirportStatisticsResponse> result = airportStatistics.execute();

        assertTrue(result.isEmpty());
    }

    @Test
    void ensureRoutesOfEveryReplicaAreCountedForBothAirportsMostConnectedFirst() {
        when(airports.findAll()).thenReturn(List.of(airport("OPO", "Porto"), airport("LIS", "Lisbon"), airport("FAO", "Faro")));
        when(routes.findAll()).thenReturn(List.of(route("LIS", "OPO"), route("OPO", "LIS"), route("LIS", "FAO")));

        List<AirportStatisticsResponse> result = airportStatistics.execute();

        assertEquals(List.of("LIS", "OPO", "FAO"), result.stream().map(AirportStatisticsResponse::iataCode).toList());
        assertEquals(List.of(3L, 2L, 1L), result.stream().map(AirportStatisticsResponse::routeCount).toList());
    }

    @Test
    void ensureAirportWithoutRoutesCountsZero() {
        when(airports.findAll()).thenReturn(List.of(airport("LIS", "Lisbon")));
        when(routes.findAll()).thenReturn(List.of());

        List<AirportStatisticsResponse> result = airportStatistics.execute();

        assertEquals(1, result.size());
        assertEquals(0L, result.get(0).routeCount());
    }
}
