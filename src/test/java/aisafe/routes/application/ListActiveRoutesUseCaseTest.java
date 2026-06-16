package aisafe.routes.application;

import aisafe.routes.application.dtos.ActiveRouteResponse;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import aisafe.flights.domain.ScheduledFlightRepository;
import aisafe.airports.domain.IataCode;
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
}
