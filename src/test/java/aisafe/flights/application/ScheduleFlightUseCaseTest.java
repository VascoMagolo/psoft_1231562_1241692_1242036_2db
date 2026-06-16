package aisafe.flights.application;

import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.airports.domain.IataCode;
import aisafe.flights.application.dtos.FlightResponse;
import aisafe.flights.application.dtos.ScheduleFlightRequest;
import aisafe.flights.domain.ScheduledFlight;
import aisafe.flights.domain.ScheduledFlightRepository;
import aisafe.routes.application.RouteDistanceService;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleFlightUseCaseTest {

    @Mock
    private ScheduledFlightRepository scheduledFlightRepository;
    @Mock
    private RouteRepository routeRepository;
    @Mock
    private AircraftRepository aircraftRepository;
    @Mock
    private RouteDistanceService routeDistanceService;

    @InjectMocks
    private ScheduleFlightUseCase useCase;

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void ensureFlightIsScheduledSuccessfully() {
        OffsetDateTime departure = OffsetDateTime.now().plusDays(1);
        OffsetDateTime arrival = departure.plusHours(2);
        ScheduleFlightRequest request = new ScheduleFlightRequest("CS-TPA", "OPO", "LIS", departure, arrival);

        Route route = mock(Route.class);
        when(route.getOrigin()).thenReturn(new IataCode("OPO"));
        when(route.getDestination()).thenReturn(new IataCode("LIS"));
        
        Aircraft aircraft = mock(Aircraft.class);
        when(aircraft.getRegistrationNumber()).thenReturn(new RegistrationNumber("CS-TPA"));
        when(aircraft.getRange()).thenReturn(2000.0);

        when(routeRepository.findByOriginAndDestination(any(), any())).thenReturn(Optional.of(route));
        when(aircraftRepository.findByRegistrationNumber(any())).thenReturn(Optional.of(aircraft));
        when(routeDistanceService.calculateDistanceKm(route)).thenReturn(300.0);

        FlightResponse result = useCase.execute(request);

        assertNotNull(result);
        assertEquals("CS-TPA", result.aircraftId());
        verify(scheduledFlightRepository).save(any(ScheduledFlight.class));
    }
}
