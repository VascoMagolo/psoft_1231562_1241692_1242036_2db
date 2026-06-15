package aisafe.routes.application;

import aisafe.airports.domain.AirportRepository;
import aisafe.routes.application.dtos.AlternativeRouteResponse;
import aisafe.routes.domain.InvalidRouteException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchAlternativeRoutesUseCaseTest {

    @Mock
    private AirportRepository airportRepository;
    @Mock
    private NetworkGraphService networkGraphService;

    @InjectMocks
    private SearchAlternativeRoutesUseCase useCase;

    @Test
    void executeReturnsAlternativeRoutes() {
        when(airportRepository.existsByIataCode(any())).thenReturn(true);
        when(networkGraphService.findAlternativePaths("OPO", "LIS")).thenReturn(List.of(List.of("OPO", "MAD", "LIS")));

        List<AlternativeRouteResponse> result = useCase.execute("OPO", "LIS");

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).numberOfStops());
    }

    @Test
    void executeWithSameOriginAndDestinationThrowsException() {
        assertThrows(InvalidRouteException.class, () -> useCase.execute("OPO", "OPO"));
    }
}
