package aisafe.routes.application;

import aisafe.airports.domain.*;
import aisafe.routes.application.dtos.CreateRouteRequest;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteHistory;
import aisafe.routes.domain.RouteHistoryRepository;
import aisafe.routes.domain.RouteRepository;
import aisafe.shared.domain.DuplicateResourceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateRouteUseCaseTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private AirportRepository airportRepository;

    @Mock
    private RouteHistoryRepository routeHistoryRepository;

    @InjectMocks
    private CreateRouteUseCase createRoute;

    private Airport createSampleAirport(String code) {
        return new Airport(code, code + " Name", "City", "Country", "Region", "UTC", 0.0, 0.0, List.of(new Runway("RWY1", 2000, "12/30")));
    }

    @Test
    void ensureRouteIsCreatedSuccessfully() {
        CreateRouteRequest request = new CreateRouteRequest("OPO", "LIS", 45, 300.0, 150, "admin");

        when(airportRepository.findByIataCode(new IataCode("OPO"))).thenReturn(Optional.of(createSampleAirport("OPO")));
        when(airportRepository.findByIataCode(new IataCode("LIS"))).thenReturn(Optional.of(createSampleAirport("LIS")));
        when(routeRepository.existsByOriginAndDestination(any(IataCode.class), any(IataCode.class))).thenReturn(false);

        var result = createRoute.execute(request);

        assertNotNull(result);
        assertEquals("OPO", result.originIataCode());
        assertEquals("LIS", result.destinationIataCode());
        verify(routeRepository, times(1)).save(any(Route.class));
        verify(routeHistoryRepository, times(1)).save(any(RouteHistory.class));
    }

    @Test
    void ensureExceptionThrownWhenOriginAirportNotFound() {
        CreateRouteRequest request = new CreateRouteRequest("XXX", "LIS", 45, 300.0, 150, "admin");

        when(airportRepository.findByIataCode(new IataCode("XXX"))).thenReturn(Optional.empty());

        assertThrows(AirportNotFoundException.class, () -> createRoute.execute(request));
        verify(routeRepository, never()).save(any());
    }

    @Test
    void ensureExceptionThrownWhenDestinationAirportNotFound() {
        CreateRouteRequest request = new CreateRouteRequest("OPO", "XXX", 45, 300.0, 150, "admin");

        when(airportRepository.findByIataCode(new IataCode("OPO"))).thenReturn(Optional.of(createSampleAirport("OPO")));
        when(airportRepository.findByIataCode(new IataCode("XXX"))).thenReturn(Optional.empty());

        assertThrows(AirportNotFoundException.class, () -> createRoute.execute(request));
        verify(routeRepository, never()).save(any());
    }

    @Test
    void ensureExceptionThrownWhenRouteAlreadyExists() {
        CreateRouteRequest request = new CreateRouteRequest("OPO", "LIS", 45, 300.0, 150, "admin");

        when(airportRepository.findByIataCode(new IataCode("OPO"))).thenReturn(Optional.of(createSampleAirport("OPO")));
        when(airportRepository.findByIataCode(new IataCode("LIS"))).thenReturn(Optional.of(createSampleAirport("LIS")));
        when(routeRepository.existsByOriginAndDestination(any(IataCode.class), any(IataCode.class))).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () -> createRoute.execute(request));
        assertEquals("Route already exists between origin and destination.", ex.getMessage());
        verify(routeRepository, never()).save(any());
    }

    @Test
    void ensureExceptionThrownWhenAirportNotOperational() {
        CreateRouteRequest request = new CreateRouteRequest("OPO", "LIS", 45, 300.0, 150, "admin");
        Airport opo = createSampleAirport("OPO");
        opo.changeStatus(AirportStatus.CLOSED);

        when(airportRepository.findByIataCode(new IataCode("OPO"))).thenReturn(Optional.of(opo));

        assertThrows(aisafe.routes.domain.InvalidRouteException.class, () -> createRoute.execute(request));
        verify(routeRepository, never()).save(any());
    }
}
