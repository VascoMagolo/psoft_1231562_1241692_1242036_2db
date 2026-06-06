package aisafe.routes.application;

import aisafe.airports.domain.IataCode;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import aisafe.shared.domain.PaginatedResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
        when(routeRepository.findByOriginAndDestination(any(), any(), anyInt(), anyInt()))
                .thenReturn(new PaginatedResult<>(List.of(sample), 1L));

        PaginatedResult<Route> result = searchRoutes.execute("OPO", "LIS", 0, 20);

        assertEquals(1L, result.totalElements());
        verify(routeRepository).findByOriginAndDestination(any(), any(), anyInt(), anyInt());
    }

    @Test
    void ensureSearchByOriginOnly() {
        when(routeRepository.findByOrigin(any(IataCode.class), anyInt(), anyInt()))
                .thenReturn(new PaginatedResult<>(List.of(sample), 1L));

        PaginatedResult<Route> result = searchRoutes.execute("OPO", null, 0, 20);

        assertEquals(1L, result.totalElements());
        verify(routeRepository).findByOrigin(any(IataCode.class), anyInt(), anyInt());
    }

    @Test
    void ensureSearchByDestinationOnly() {
        when(routeRepository.findByDestination(any(IataCode.class), anyInt(), anyInt()))
                .thenReturn(new PaginatedResult<>(List.of(sample), 1L));

        PaginatedResult<Route> result = searchRoutes.execute(null, "LIS", 0, 20);

        assertEquals(1L, result.totalElements());
        verify(routeRepository).findByDestination(any(IataCode.class), anyInt(), anyInt());
    }

    @Test
    void ensureSearchWithNoParamsReturnsAll() {
        when(routeRepository.findAll(anyInt(), anyInt()))
                .thenReturn(new PaginatedResult<>(List.of(sample), 1L));

        PaginatedResult<Route> result = searchRoutes.execute(null, null, 0, 20);

        assertEquals(1L, result.totalElements());
        verify(routeRepository).findAll(anyInt(), anyInt());
    }
}
