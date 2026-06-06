package aisafe.routes.application;

import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.IataCode;
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
        Page<Route> routePage = new PageImpl<>(List.of(route));
        when(airportRepository.existsByIataCodeCode("OPO")).thenReturn(true);
        when(routeRepository.findByOrigin(any(IataCode.class), any(Pageable.class))).thenReturn(routePage);

        Page<Route> result = listRoutesFromAirport.execute("OPO", Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        verify(routeRepository).findByOrigin(any(IataCode.class), any(Pageable.class));
    }

    @Test
    void ensureExceptionWhenAirportNotFound() {
        when(airportRepository.existsByIataCodeCode("XXX")).thenReturn(false);

        assertThrows(AirportNotFoundException.class, () -> listRoutesFromAirport.execute("XXX", Pageable.unpaged()));
        verify(routeRepository, never()).findByOrigin(any(), any());
    }
}
