package aisafe.routes.application;

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
class SearchRoutesUseCaseTest {

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private SearchRoutesUseCase searchRoutes;

    private final Route sample = new Route("OPO", "LIS", 45, 300.0, 150);

    @Test
    void ensureSearchByOriginAndDestination() {
        when(routeRepository.findByOriginAndDestination(any(), any())).thenReturn(List.of(sample));

        List<Route> result = searchRoutes.execute("OPO", "LIS");

        assertEquals(1, result.size());
        verify(routeRepository).findByOriginAndDestination(any(), any());
    }

    @Test
    void ensureSearchByOriginOnly() {
        when(routeRepository.findByOrigin(any())).thenReturn(List.of(sample));

        List<Route> result = searchRoutes.execute("OPO", null);

        assertEquals(1, result.size());
        verify(routeRepository).findByOrigin(any());
    }

    @Test
    void ensureSearchByDestinationOnly() {
        when(routeRepository.findByDestination(any())).thenReturn(List.of(sample));

        List<Route> result = searchRoutes.execute(null, "LIS");

        assertEquals(1, result.size());
        verify(routeRepository).findByDestination(any());
    }

    @Test
    void ensureSearchWithNoParamsReturnsAll() {
        when(routeRepository.findAll()).thenReturn(List.of(sample));

        List<Route> result = searchRoutes.execute(null, null);

        assertEquals(1, result.size());
        verify(routeRepository).findAll();
    }
}
