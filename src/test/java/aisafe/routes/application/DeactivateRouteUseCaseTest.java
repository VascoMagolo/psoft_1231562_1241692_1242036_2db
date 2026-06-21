package aisafe.routes.application;

import aisafe.airports.domain.IataCode;
import aisafe.routes.application.dtos.RouteResponse;
import aisafe.routes.domain.*;
import aisafe.shared.domain.ConcurrencyException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeactivateRouteUseCaseTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private RouteHistoryRepository routeHistoryRepository;

    @InjectMocks
    private DeactivateRouteUseCase deactivateRoute;

    @Test
    void ensureRouteIsDeactivatedSuccessfully() {
        Route route = new Route("OPO", "LIS", 45, 300.0, 150);
        when(routeRepository.findByOriginAndDestination(any(IataCode.class), any(IataCode.class)))
                .thenReturn(Optional.of(route));

        RouteResponse result = deactivateRoute.execute("OPO", "LIS", null, "testuser");

        assertEquals(RouteStatus.INACTIVE, result.status());
        verify(routeHistoryRepository).save(any(RouteHistory.class));
    }

    @Test
    void ensureVersionMismatchThrowsOptimisticLockException() {
        Route route = new Route("OPO", "LIS", 45, 300.0, 150);
        when(routeRepository.findByOriginAndDestination(any(IataCode.class), any(IataCode.class)))
                .thenReturn(Optional.of(route));

        assertThrows(ConcurrencyException.class, () ->
                deactivateRoute.execute("OPO", "LIS", 1L, "testuser"));
        verify(routeRepository, never()).save(any());
    }

    @Test
    void ensureExceptionWhenRouteNotFound() {
        when(routeRepository.findByOriginAndDestination(any(IataCode.class), any(IataCode.class)))
                .thenReturn(Optional.empty());

        assertThrows(RouteNotFoundException.class, () -> deactivateRoute.execute("OPO", "LIS", null, "testuser"));
        verify(routeRepository, never()).save(any());
    }

    @Test
    void ensureRouteIsDeactivatedSuccessfullyWithVersion() {
        Route route = new Route("OPO", "LIS", 45, 300.0, 150);
        IataCode origin = new IataCode("OPO");
        IataCode dest = new IataCode("LIS");
        when(routeRepository.findByOriginAndDestination(any(IataCode.class), any(IataCode.class)))
                .thenReturn(Optional.of(route));
        when(routeRepository.findVersionFor(origin, dest)).thenReturn(1L);

        RouteResponse result = deactivateRoute.execute("OPO", "LIS", 1L, "testuser");

        assertEquals(RouteStatus.INACTIVE, result.status());
        verify(routeRepository).save(route);
    }
}
