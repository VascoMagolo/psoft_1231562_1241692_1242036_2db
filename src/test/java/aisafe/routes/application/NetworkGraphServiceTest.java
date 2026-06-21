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
        // OPO -> LIS -> OPO (cycle), LIS -> MAD
        Route r1 = mock(Route.class);
        when(r1.getOrigin()).thenReturn(new IataCode("OPO"));
        when(r1.getDestination()).thenReturn(new IataCode("LIS"));

        Route r2 = mock(Route.class);
        when(r2.getOrigin()).thenReturn(new IataCode("LIS"));
        when(r2.getDestination()).thenReturn(new IataCode("OPO"));

        Route r3 = mock(Route.class);
        when(r3.getOrigin()).thenReturn(new IataCode("LIS"));
        when(r3.getDestination()).thenReturn(new IataCode("MAD"));

        when(routeRepository.findAllActive()).thenReturn(List.of(r1, r2, r3));

        List<List<String>> paths = service.findAlternativePaths("OPO", "MAD");

        assertEquals(1, paths.size());
        assertEquals(List.of("OPO", "LIS", "MAD"), paths.get(0));
    }

    @Test
    void findAlternativePathsHandlesMaxDepth() {
        // AAA -> BBB -> CCC -> DDD -> EEE -> FFF -> GGG (depth 7 > 5)
        Route r1 = mock(Route.class);
        when(r1.getOrigin()).thenReturn(new IataCode("AAA"));
        when(r1.getDestination()).thenReturn(new IataCode("BBB"));

        Route r2 = mock(Route.class);
        when(r2.getOrigin()).thenReturn(new IataCode("BBB"));
        when(r2.getDestination()).thenReturn(new IataCode("CCC"));

        Route r3 = mock(Route.class);
        when(r3.getOrigin()).thenReturn(new IataCode("CCC"));
        when(r3.getDestination()).thenReturn(new IataCode("DDD"));

        Route r4 = mock(Route.class);
        when(r4.getOrigin()).thenReturn(new IataCode("DDD"));
        when(r4.getDestination()).thenReturn(new IataCode("EEE"));

        Route r5 = mock(Route.class);
        when(r5.getOrigin()).thenReturn(new IataCode("EEE"));
        when(r5.getDestination()).thenReturn(new IataCode("FFF"));

        Route r6 = mock(Route.class);
        when(r6.getOrigin()).thenReturn(new IataCode("FFF"));
        when(r6.getDestination()).thenReturn(new IataCode("GGG"));

        when(routeRepository.findAllActive()).thenReturn(List.of(r1, r2, r3, r4, r5, r6));

        List<List<String>> paths = service.findAlternativePaths("AAA", "GGG");

        // The path length is 7 nodes. Max depth limit is 5.
        // It should return no paths because AAA -> BBB -> CCC -> DDD -> EEE -> FFF is size 6 (> 5), returning early.
        assertTrue(paths.isEmpty());
    }
}
