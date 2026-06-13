package aisafe.routes.application;

import aisafe.airports.domain.IataCode;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteNotFoundException;
import aisafe.routes.domain.RouteRepository;
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
class ViewRouteDetailsUseCaseTest {

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private ViewRouteDetailsUseCase viewRouteDetails;

    @Test
    void ensureRouteDetailsReturnedSuccessfully() {
        Route route = new Route("OPO", "LIS", 45, 300.0, 150);
        when(routeRepository.findByOriginAndDestination(any(IataCode.class), any(IataCode.class)))
                .thenReturn(Optional.of(route));

        Route result = viewRouteDetails.execute("OPO", "LIS");

        assertNotNull(result);
        verify(routeRepository).findByOriginAndDestination(any(IataCode.class), any(IataCode.class));
    }

    @Test
    void ensureExceptionWhenRouteNotFound() {
        when(routeRepository.findByOriginAndDestination(any(IataCode.class), any(IataCode.class)))
                .thenReturn(Optional.empty());

        assertThrows(RouteNotFoundException.class, () -> viewRouteDetails.execute("OPO", "LIS"));
    }
}
