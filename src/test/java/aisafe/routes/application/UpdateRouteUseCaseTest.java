package aisafe.routes.application;

import aisafe.routes.application.dtos.UpdateRouteRequest;
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
class UpdateRouteUseCaseTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private RouteHistoryRepository routeHistoryRepository;

    @InjectMocks
    private UpdateRouteUseCase updateRoute;

    @Test
    void ensureRouteIsUpdatedSuccessfully() {
        Route route = new Route("OPO", "LIS", 45, 300.0, 150);
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(routeRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(routeHistoryRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        UpdateRouteRequest request = new UpdateRouteRequest(60, 400.0, 180, null);
        Route result = updateRoute.execute(1L, request);

        assertEquals(60, result.getEstimatedFlightTime());
        verify(routeHistoryRepository).save(any(RouteHistory.class));
    }

    @Test
    void ensureExceptionWhenRouteNotFound() {
        when(routeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RouteNotFoundException.class, () ->
                updateRoute.execute(99L, new UpdateRouteRequest(45, 300.0, 150, null)));
        verify(routeRepository, never()).save(any());
    }
}
