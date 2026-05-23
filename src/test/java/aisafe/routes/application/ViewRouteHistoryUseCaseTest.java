package aisafe.routes.application;

import aisafe.routes.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViewRouteHistoryUseCaseTest {

    @Mock
    private RouteHistoryRepository historyRepository;

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private ViewRouteHistoryUseCase viewRouteHistory;

    @Test
    void ensureHistoryIsReturnedSuccessfully() {
        Route route = new Route("OPO", "LIS", 45, 300.0, 150);
        RouteHistory entry = new RouteHistory(route, "Route created", "system");
        when(routeRepository.existsById(1L)).thenReturn(true);
        when(historyRepository.findAllByRouteId(1L)).thenReturn(List.of(entry));

        List<RouteHistory> result = viewRouteHistory.execute(1L);

        assertEquals(1, result.size());
        assertEquals("Route created", result.get(0).getChangeDescription());
    }

    @Test
    void ensureExceptionWhenRouteNotFound() {
        when(routeRepository.existsById(99L)).thenReturn(false);

        assertThrows(RouteNotFoundException.class, () -> viewRouteHistory.execute(99L));
        verify(historyRepository, never()).findAllByRouteId(any());
    }
}
