package aisafe.airports.application;

import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.Runway;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import aisafe.shared.domain.ResourceInUseException;
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
class DeleteAirportUseCaseTest {

    @Mock
    private AirportRepository airportRepository;

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private DeleteAirportUseCase deleteAirport;

    private Airport buildAirport(String iataCode) {
        return new Airport(iataCode, "Lisbon Airport", "Lisbon", "Portugal", "Europe",
                "Europe/Lisbon", 38.77, -9.13,
                List.of(new Runway("03/21", 3000, "030/210")));
    }

    @Test
    void ensureAirportIsDeletedSuccessfully() {
        Airport airport = buildAirport("LIS");
        when(airportRepository.findByIataCodeCode("LIS")).thenReturn(Optional.of(airport));
        when(routeRepository.findByOriginOrDestination(any(), any())).thenReturn(List.of());

        assertDoesNotThrow(() -> deleteAirport.execute("LIS"));
        verify(airportRepository).delete(any(Airport.class));
    }

    @Test
    void ensureExceptionWhenAirportNotFound() {
        when(airportRepository.findByIataCodeCode("XXX")).thenReturn(Optional.empty());

        assertThrows(AirportNotFoundException.class, () -> deleteAirport.execute("XXX"));
        verify(airportRepository, never()).delete(any());
    }

    @Test
    void ensureExceptionWhenAirportHasActiveRoutes() {
        Airport airport = buildAirport("LIS");
        Route route = new Route("LIS", "OPO", 45, 300.0, 100);
        when(airportRepository.findByIataCodeCode("LIS")).thenReturn(Optional.of(airport));
        when(routeRepository.findByOriginOrDestination(any(), any())).thenReturn(List.of(route));

        assertThrows(ResourceInUseException.class, () -> deleteAirport.execute("LIS"));
        verify(airportRepository, never()).delete(any());
    }
}
