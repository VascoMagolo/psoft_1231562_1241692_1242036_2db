package aisafe.airports.application;

import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.IataCode;
import aisafe.routes.application.dtos.RouteResponse;
import aisafe.routes.domain.RouteSummaryData;
import aisafe.routes.domain.RouteRepository;
import aisafe.routes.domain.RouteStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViewAirportRoutesUseCaseTest {

    @Mock
    private AirportRepository airportRepository;

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private ViewAirportRoutesUseCase viewAirportRoutes;

    @Test
    void ensureRoutesReturnedForExistingAirport() {
        RouteSummaryData summary = new RouteSummaryData(
                new IataCode("LIS"), new IataCode("OPO"), 45, 300.0, 150, RouteStatus.ACTIVE, 7L);
        when(airportRepository.existsByIataCode(new IataCode("LIS"))).thenReturn(true);
        when(routeRepository.listSummariesForAirport(any(IataCode.class))).thenReturn(List.of(summary));

        List<RouteResponse> result = viewAirportRoutes.execute("LIS");

        assertEquals(1, result.size());
        assertEquals(7L, result.get(0).version());
        verify(routeRepository).listSummariesForAirport(any(IataCode.class));
        verify(routeRepository, never()).findVersionFor(any(), any());
    }

    @Test
    void ensureExceptionWhenAirportNotFound() {
        when(airportRepository.existsByIataCode(new IataCode("XXX"))).thenReturn(false);

        assertThrows(AirportNotFoundException.class, () -> viewAirportRoutes.execute("XXX"));
        verify(routeRepository, never()).listSummariesForAirport(any());
    }
}
