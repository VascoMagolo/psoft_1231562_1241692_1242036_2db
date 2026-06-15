package aisafe.routes.application;

import aisafe.airports.domain.IataCode;
import aisafe.routes.domain.*;
import aisafe.shared.domain.ConcurrencyException;
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
    void ensureRouteIsDeactivatedSuccessfully() {
        Route route = new Route("OPO", "LIS", 45, 300.0, 150);
        when(routeRepository.findByOriginAndDestination(any(IataCode.class), any(IataCode.class)))
                .thenReturn(Optional.of(route));

        Route result = deactivateRoute.execute("OPO", "LIS", null);

        assertEquals(RouteStatus.INACTIVE, result.getStatus());
        verify(routeHistoryRepository).save(any(RouteHistory.class));
    }

    @Test
    void ensureVersionMismatchThrowsOptimisticLockException() {
        Route route = new Route("OPO", "LIS", 45, 300.0, 150);
        when(routeRepository.findByOriginAndDestination(any(IataCode.class), any(IataCode.class)))
                .thenReturn(Optional.of(route));

        assertThrows(ConcurrencyException.class, () ->
                deactivateRoute.execute("OPO", "LIS", 1L));
        verify(routeRepository, never()).save(any());
    }

    @Test
    void ensureExceptionWhenRouteNotFound() {
        when(routeRepository.findByOriginAndDestination(any(IataCode.class), any(IataCode.class)))
                .thenReturn(Optional.empty());

        assertThrows(RouteNotFoundException.class, () -> deactivateRoute.execute("OPO", "LIS", null));
        verify(routeRepository, never()).save(any());
    }
}
