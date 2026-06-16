package aisafe.routes.application;

import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import aisafe.airports.domain.IataCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NetworkGraphServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private NetworkGraphService service;

    @Test
    void findAlternativePathsReturnsCorrectPaths() {
        // A -> B -> C
        // A -> C (direct)
        Route r1 = mock(Route.class);
        when(r1.getOrigin()).thenReturn(new IataCode("OPO"));
        when(r1.getDestination()).thenReturn(new IataCode("LIS"));
        
        Route r2 = mock(Route.class);
        when(r2.getOrigin()).thenReturn(new IataCode("LIS"));
        when(r2.getDestination()).thenReturn(new IataCode("MAD"));
        
        Route r3 = mock(Route.class);
        when(r3.getOrigin()).thenReturn(new IataCode("OPO"));
        when(r3.getDestination()).thenReturn(new IataCode("MAD"));

        when(routeRepository.findAllActive()).thenReturn(List.of(r1, r2, r3));

        List<List<String>> paths = service.findAlternativePaths("OPO", "MAD");

        // Should return [OPO, LIS, MAD] because [OPO, MAD] is direct (size 2) and filter removes size <= 2
        assertEquals(1, paths.size());
        assertEquals(List.of("OPO", "LIS", "MAD"), paths.get(0));
    }

    @Test
    void findAlternativePathsDetectsCycles() {
        // A -> B -> A
        Route r1 = mock(Route.class);
        when(r1.getOrigin()).thenReturn(new IataCode("OPO"));
        when(r1.getDestination()).thenReturn(new IataCode("LIS"));
        
        Route r2 = mock(Route.class);
        when(r2.getOrigin()).thenReturn(new IataCode("LIS"));
        when(r2.getDestination()).thenReturn(new IataCode("OPO"));

        when(routeRepository.findAllActive()).thenReturn(List.of(r1, r2));

        List<List<String>> paths = service.findAlternativePaths("OPO", "LIS");

        // Should only return direct path [OPO, LIS] which is filtered out
        assertTrue(paths.isEmpty());
    }
}
