package aisafe.routes.application;

import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
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
class ListRoutesFromAirportUseCaseTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private ListRoutesFromAirportUseCase listRoutesFromAirport;

    @Test
    void ensureRoutesReturnedForExistingAirport() {
        Route route = new Route("OPO", "LIS", 45, 300.0, 150);
        when(airportRepository.existsByIataCodeCode("OPO")).thenReturn(true);
        when(routeRepository.findByOrigin(any(IataCode.class), anyInt(), anyInt()))
                .thenReturn(new PaginatedResult<>(List.of(route), 1L));

        PaginatedResult<Route> result = listRoutesFromAirport.execute("OPO", 0, 20);

        assertEquals(1L, result.totalElements());
        verify(routeRepository).findByOrigin(any(IataCode.class), anyInt(), anyInt());
    }

    @Test
    void ensureExceptionWhenAirportNotFound() {
        when(airportRepository.existsByIataCodeCode("XXX")).thenReturn(false);

        assertThrows(AirportNotFoundException.class, () -> listRoutesFromAirport.execute("XXX", 0, 20));
        verify(routeRepository, never()).findByOrigin(any(), anyInt(), anyInt());
    }
}
