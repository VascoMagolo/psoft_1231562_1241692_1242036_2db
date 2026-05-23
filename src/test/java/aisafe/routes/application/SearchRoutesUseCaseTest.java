package aisafe.routes.application;

import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
    private final Pageable pageable = Pageable.unpaged();

    @Test
    void ensureSearchByOriginAndDestination() {
        when(routeRepository.findByOriginAndDestination(any(), any(), any())).thenReturn(new PageImpl<>(List.of(sample)));

        Page<Route> result = searchRoutes.execute("OPO", "LIS", pageable);

        assertEquals(1, result.getTotalElements());
        verify(routeRepository).findByOriginAndDestination(any(), any(), any());
    }

    @Test
    void ensureSearchByOriginOnly() {
        when(routeRepository.findByOrigin(any(), any())).thenReturn(new PageImpl<>(List.of(sample)));

        Page<Route> result = searchRoutes.execute("OPO", null, pageable);

        assertEquals(1, result.getTotalElements());
        verify(routeRepository).findByOrigin(any(), any());
    }

    @Test
    void ensureSearchByDestinationOnly() {
        when(routeRepository.findByDestination(any(), any())).thenReturn(new PageImpl<>(List.of(sample)));

        Page<Route> result = searchRoutes.execute(null, "LIS", pageable);

        assertEquals(1, result.getTotalElements());
        verify(routeRepository).findByDestination(any(), any());
    }

    @Test
    void ensureSearchWithNoParamsReturnsAll() {
        when(routeRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(sample)));

        Page<Route> result = searchRoutes.execute(null, null, pageable);

        assertEquals(1, result.getTotalElements());
        verify(routeRepository).findAll(any(Pageable.class));
    }
}
