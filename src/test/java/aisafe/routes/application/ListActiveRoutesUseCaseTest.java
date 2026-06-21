package aisafe.routes.application;

import aisafe.routes.application.dtos.ActiveRouteResponse;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import aisafe.flights.domain.ScheduledFlightRepository;
import aisafe.airports.domain.IataCode;
import aisafe.shared.application.RouteDistanceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListActiveRoutesUseCaseTest {

    @Mock
    private RouteRepository routeRepository;
    @Mock
    private ScheduledFlightRepository scheduledFlightRepository;
    @Mock
    private RouteDistanceService routeDistanceService;

    @InjectMocks
    private ListActiveRoutesUseCase useCase;

    @Test
    void executeReturnsActiveRoutes() {
        Route route = mock(Route.class);
        when(route.getOrigin()).thenReturn(new IataCode("OPO"));
        when(route.getDestination()).thenReturn(new IataCode("LIS"));
        
        when(routeRepository.findAllActive()).thenReturn(List.of(route));
        when(routeDistanceService.calculateDistanceKm(route)).thenReturn(300.0);
        when(scheduledFlightRepository.countByRoute(any(), any())).thenReturn(5L);

        List<ActiveRouteResponse> result = useCase.execute("active", "distance");

        assertEquals(1, result.size());
        assertEquals("OPO", result.get(0).originIataCode());
        assertEquals(300.0, result.get(0).distanceKm());
        assertEquals(5L, result.get(0).popularity());
    }

    @Test
    void ensureExecuteThrowsInvalidSortParameterExceptionWhenSortByIsInvalid() {
        assertThrows(aisafe.routes.domain.InvalidSortParameterException.class, () -> {
            useCase.execute("active", "invalidSort");
        });
    }

    @Test
    void ensureExecuteThrowsInvalidListingCriteriaExceptionWhenStatusIsInvalid() {
        assertThrows(aisafe.shared.domain.InvalidListingCriteriaException.class, () -> {
            useCase.execute("inactive", "distance");
        });
    }

    @Test
    void ensureExecuteSortsByPopularityCorrectly() {
        Route route1 = mock(Route.class);
        when(route1.getOrigin()).thenReturn(new IataCode("OPO"));
        when(route1.getDestination()).thenReturn(new IataCode("LIS"));

        Route route2 = mock(Route.class);
        when(route2.getOrigin()).thenReturn(new IataCode("LIS"));
        when(route2.getDestination()).thenReturn(new IataCode("MAD"));

        when(routeRepository.findAllActive()).thenReturn(List.of(route1, route2));
        when(routeDistanceService.calculateDistanceKm(route1)).thenReturn(300.0);
        when(routeDistanceService.calculateDistanceKm(route2)).thenReturn(500.0);
        when(scheduledFlightRepository.countByRoute(route1.getOrigin(), route1.getDestination())).thenReturn(5L);
        when(scheduledFlightRepository.countByRoute(route2.getOrigin(), route2.getDestination())).thenReturn(10L);

        // Popularity: route2 has 10L, route1 has 5L. Reversed order: route2, then route1
        List<ActiveRouteResponse> result = useCase.execute("active", "popularity");

        assertEquals(2, result.size());
        assertEquals("LIS", result.get(0).originIataCode()); // route2
        assertEquals("OPO", result.get(1).originIataCode()); // route1
    }

    @Test
    void ensureExecuteDefaultsToDistanceWhenSortByIsNull() {
        Route route1 = mock(Route.class);
        when(route1.getOrigin()).thenReturn(new IataCode("OPO"));
        when(route1.getDestination()).thenReturn(new IataCode("LIS"));

        Route route2 = mock(Route.class);
        when(route2.getOrigin()).thenReturn(new IataCode("LIS"));
        when(route2.getDestination()).thenReturn(new IataCode("MAD"));

        when(routeRepository.findAllActive()).thenReturn(List.of(route1, route2));
        when(routeDistanceService.calculateDistanceKm(route1)).thenReturn(300.0);
        when(routeDistanceService.calculateDistanceKm(route2)).thenReturn(100.0);
        when(scheduledFlightRepository.countByRoute(any(), any())).thenReturn(5L);

        // Distance: route2 has 100.0, route1 has 300.0. Sorted: route2, then route1
        List<ActiveRouteResponse> result = useCase.execute(null, null);

        assertEquals(2, result.size());
        assertEquals("LIS", result.get(0).originIataCode()); // route2
        assertEquals("OPO", result.get(1).originIataCode()); // route1
    }
}
