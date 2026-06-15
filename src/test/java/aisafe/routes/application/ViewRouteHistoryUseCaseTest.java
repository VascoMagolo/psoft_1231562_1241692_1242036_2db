package aisafe.routes.application;

import aisafe.airports.domain.IataCode;
import aisafe.routes.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
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
        RouteHistory entry = new RouteHistory("OPO", "LIS", "Route created", "system");
        when(routeRepository.existsByOriginAndDestination(any(IataCode.class), any(IataCode.class))).thenReturn(true);
        when(historyRepository.findAllByRoute("OPO", "LIS")).thenReturn(List.of(entry));

        List<RouteHistory> result = viewRouteHistory.execute("OPO", "LIS");

        assertEquals(1, result.size());
        assertEquals("Route created", result.get(0).getChangeDescription());
    }

    @Test
    void ensureExceptionWhenRouteNotFound() {
        when(routeRepository.existsByOriginAndDestination(any(IataCode.class), any(IataCode.class))).thenReturn(false);

        assertThrows(RouteNotFoundException.class, () -> viewRouteHistory.execute("OPO", "LIS"));
        verify(historyRepository, never()).findAllByRoute(anyString(), anyString());
    }
}
