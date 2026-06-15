package aisafe.routes.application;

import aisafe.routes.application.dtos.TotalDistanceResponse;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculateTotalNetworkDistanceUseCaseTest {

    @Mock
    private RouteRepository routeRepository;
    @Mock
    private RouteDistanceService routeDistanceService;

    @InjectMocks
    private CalculateTotalNetworkDistanceUseCase useCase;

    @Test
    void executeReturnsTotalDistance() {
        Route r1 = mock(Route.class);
        Route r2 = mock(Route.class);
        when(routeRepository.findAllActive()).thenReturn(List.of(r1, r2));
        when(routeDistanceService.calculateDistanceKm(r1)).thenReturn(100.0);
        when(routeDistanceService.calculateDistanceKm(r2)).thenReturn(200.0);

        TotalDistanceResponse result = useCase.execute();

        assertEquals(300.0, result.totalDistance());
        assertEquals("km", result.unit());
    }
}
