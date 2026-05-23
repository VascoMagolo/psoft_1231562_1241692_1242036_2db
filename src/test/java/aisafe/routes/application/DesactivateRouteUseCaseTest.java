package aisafe.routes.application;

import aisafe.routes.domain.*;
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
class DesactivateRouteUseCaseTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private RouteHistoryRepository routeHistoryRepository;

    @InjectMocks
    private DesactivateRouteUseCase desactivateRoute;

    @Test
    void ensureRouteIsDeactivatedSuccessfully() {
        Route route = new Route("OPO", "LIS", 45, 300.0, 150);
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(routeRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(routeHistoryRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Route result = desactivateRoute.execute(1L);

        assertFalse(result.isActive());
        verify(routeHistoryRepository).save(any(RouteHistory.class));
    }

    @Test
    void ensureExceptionWhenRouteNotFound() {
        when(routeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RouteNotFoundException.class, () -> desactivateRoute.execute(99L));
        verify(routeRepository, never()).save(any());
    }
}
