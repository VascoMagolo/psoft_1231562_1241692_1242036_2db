package aisafe.routes.application;

import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.IataCode;
import aisafe.routes.application.dtos.CreateRouteRequest;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteHistory;
import aisafe.routes.domain.RouteHistoryRepository;
import aisafe.routes.domain.RouteRepository;
import aisafe.shared.domain.DuplicateResourceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateRouteUseCaseTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private AirportRepository airportRepository;

    @Mock
    private RouteHistoryRepository routeHistoryRepository;

    @InjectMocks
    private CreateRouteUseCase createRoute;

    @BeforeEach
    void setUpSecurityContext() {
        var auth = new UsernamePasswordAuthenticationToken("testuser", null, List.of());
        var ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ensureRouteIsCreatedSuccessfully() {
        CreateRouteRequest request = new CreateRouteRequest("OPO", "LIS", 45, 300.0, 150);

        when(airportRepository.existsByIataCodeCode("OPO")).thenReturn(true);
        when(airportRepository.existsByIataCodeCode("LIS")).thenReturn(true);
        when(routeRepository.existsByOriginAndDestination(any(IataCode.class), any(IataCode.class))).thenReturn(false);

        Route result = createRoute.execute(request);

        assertNotNull(result);
        assertEquals("OPO", result.getOrigin().getCode());
        assertEquals("LIS", result.getDestination().getCode());
        verify(routeRepository, times(1)).save(any(Route.class));
        verify(routeHistoryRepository, times(1)).save(any(RouteHistory.class));
    }

    @Test
    void ensureExceptionThrownWhenOriginAirportNotFound() {
        CreateRouteRequest request = new CreateRouteRequest("XXX", "LIS", 45, 300.0, 150);

        when(airportRepository.existsByIataCodeCode("XXX")).thenReturn(false);

        assertThrows(AirportNotFoundException.class, () -> createRoute.execute(request));
        verify(routeRepository, never()).save(any());
    }

    @Test
    void ensureExceptionThrownWhenDestinationAirportNotFound() {
        CreateRouteRequest request = new CreateRouteRequest("OPO", "XXX", 45, 300.0, 150);

        when(airportRepository.existsByIataCodeCode("OPO")).thenReturn(true);
        when(airportRepository.existsByIataCodeCode("XXX")).thenReturn(false);

        assertThrows(AirportNotFoundException.class, () -> createRoute.execute(request));
        verify(routeRepository, never()).save(any());
    }

    @Test
    void ensureExceptionThrownWhenRouteAlreadyExists() {
        CreateRouteRequest request = new CreateRouteRequest("OPO", "LIS", 45, 300.0, 150);

        when(airportRepository.existsByIataCodeCode("OPO")).thenReturn(true);
        when(airportRepository.existsByIataCodeCode("LIS")).thenReturn(true);
        when(routeRepository.existsByOriginAndDestination(any(IataCode.class), any(IataCode.class))).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () -> createRoute.execute(request));
        assertEquals("Route already exists between origin and destination.", ex.getMessage());
        verify(routeRepository, never()).save(any());
    }
}
