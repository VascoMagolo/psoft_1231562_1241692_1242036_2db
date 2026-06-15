package aisafe.routes.application;

import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.Coordinates;
import aisafe.routes.domain.Route;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteDistanceServiceTest {

    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private RouteDistanceService service;

    @Test
    void calculateDistanceKmReturnsCorrectValue() {
        // Porto (OPO): 41.2356, -8.6781
        // Lisbon (LIS): 38.7742, -9.1342
        // Distance should be ~274 km
        
        Airport origin = mock(Airport.class);
        when(origin.getCoordinates()).thenReturn(new Coordinates(41.2356, -8.6781));
        
        Airport destination = mock(Airport.class);
        when(destination.getCoordinates()).thenReturn(new Coordinates(38.7742, -9.1342));
        
        Route route = mock(Route.class);
        when(route.getOrigin()).thenReturn(new aisafe.airports.domain.IataCode("OPO"));
        when(route.getDestination()).thenReturn(new aisafe.airports.domain.IataCode("LIS"));

        when(airportRepository.findByIataCode(any())).thenReturn(Optional.of(origin), Optional.of(destination));

        double distance = service.calculateDistanceKm(route);

        // Allow some margin for haversine calculation
        assertTrue(distance > 270 && distance < 280, "Distance was " + distance);
    }
}
