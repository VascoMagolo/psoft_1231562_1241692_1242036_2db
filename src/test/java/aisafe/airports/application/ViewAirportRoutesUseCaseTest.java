package aisafe.airports.application;

import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.IataCode;
import aisafe.routes.application.dtos.RouteResponse;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
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
class ViewAirportRoutesUseCaseTest {

    @Mock
    private AirportRepository airportRepository;

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private ViewAirportRoutesUseCase viewAirportRoutes;

    @Test
    void ensureRoutesReturnedForExistingAirport() {
        Route route = new Route("LIS", "OPO", 45, 300.0, 150);
        when(airportRepository.existsByIataCodeCode("LIS")).thenReturn(true);
        when(routeRepository.findByOriginOrDestination(any(IataCode.class), any(IataCode.class)))
                .thenReturn(List.of(route));

        List<RouteResponse> result = viewAirportRoutes.execute("LIS");

        assertEquals(1, result.size());
        verify(routeRepository).findByOriginOrDestination(any(IataCode.class), any(IataCode.class));
    }

    @Test
    void ensureExceptionWhenAirportNotFound() {
        when(airportRepository.existsByIataCodeCode("XXX")).thenReturn(false);

        assertThrows(AirportNotFoundException.class, () -> viewAirportRoutes.execute("XXX"));
        verify(routeRepository, never()).findByOriginOrDestination(any(), any());
    }
}
