package aisafe.flights.application;

import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.airports.domain.IataCode;
import aisafe.flights.application.dtos.FlightResponse;
import aisafe.flights.application.dtos.ScheduleFlightRequest;
import aisafe.flights.domain.ScheduledFlight;
import aisafe.flights.domain.ScheduledFlightRepository;
import aisafe.shared.application.RouteDistanceService;
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
        when(route.getMinimumCapacity()).thenReturn(150);
        when(route.getMinimumRange()).thenReturn(300.0);
        
        Aircraft aircraft = mock(Aircraft.class);
        when(aircraft.getRegistrationNumber()).thenReturn(new RegistrationNumber("CS-TPA"));
        when(aircraft.getStatus()).thenReturn(aisafe.aircrafts.domain.AircraftStatus.AVAILABLE);
        when(aircraft.getSeatCapacity()).thenReturn(200);
        when(aircraft.getRange()).thenReturn(2000.0);

        when(routeRepository.findByOriginAndDestination(any(), any())).thenReturn(Optional.of(route));
        when(aircraftRepository.findByRegistrationNumber(any())).thenReturn(Optional.of(aircraft));
        when(routeDistanceService.calculateDistanceKm(route)).thenReturn(300.0);
        when(scheduledFlightRepository.save(any(ScheduledFlight.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FlightResponse result = useCase.execute(request);

        assertNotNull(result);
        assertEquals("CS-TPA", result.aircraftId());
        verify(scheduledFlightRepository).save(any(ScheduledFlight.class));
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void ensureExceptionThrownWhenAircraftCapacityInsufficient() {
        OffsetDateTime departure = OffsetDateTime.now().plusDays(1);
        OffsetDateTime arrival = departure.plusHours(2);
        ScheduleFlightRequest request = new ScheduleFlightRequest("CS-TPA", "OPO", "LIS", departure, arrival);

        Route route = mock(Route.class);
        when(route.getOrigin()).thenReturn(new IataCode("OPO"));
        when(route.getDestination()).thenReturn(new IataCode("LIS"));
        when(route.getMinimumCapacity()).thenReturn(300);
        when(route.getMinimumRange()).thenReturn(300.0);
        
        Aircraft aircraft = mock(Aircraft.class);
        when(aircraft.getRegistrationNumber()).thenReturn(new RegistrationNumber("CS-TPA"));
        when(aircraft.getStatus()).thenReturn(aisafe.aircrafts.domain.AircraftStatus.AVAILABLE);
        when(aircraft.getSeatCapacity()).thenReturn(200);
        when(aircraft.getRange()).thenReturn(2000.0);

        when(routeRepository.findByOriginAndDestination(any(), any())).thenReturn(Optional.of(route));
        when(aircraftRepository.findByRegistrationNumber(any())).thenReturn(Optional.of(aircraft));

        assertThrows(aisafe.flights.domain.AircraftIncompatibilityException.class, () -> useCase.execute(request));
        verify(scheduledFlightRepository, never()).save(any());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void ensureExceptionThrownWhenAircraftUnderMaintenance() {
        OffsetDateTime departure = OffsetDateTime.now().plusDays(1);
        OffsetDateTime arrival = departure.plusHours(2);
        ScheduleFlightRequest request = new ScheduleFlightRequest("CS-TPA", "OPO", "LIS", departure, arrival);

        Route route = mock(Route.class);
        when(route.getOrigin()).thenReturn(new IataCode("OPO"));
        when(route.getDestination()).thenReturn(new IataCode("LIS"));
        
        Aircraft aircraft = mock(Aircraft.class);
        when(aircraft.getRegistrationNumber()).thenReturn(new RegistrationNumber("CS-TPA"));
        when(aircraft.getStatus()).thenReturn(aisafe.aircrafts.domain.AircraftStatus.UNDER_MAINTENANCE);

        when(routeRepository.findByOriginAndDestination(any(), any())).thenReturn(Optional.of(route));
        when(aircraftRepository.findByRegistrationNumber(any())).thenReturn(Optional.of(aircraft));

        assertThrows(aisafe.flights.domain.AircraftUnavailableException.class, () -> useCase.execute(request));
        verify(scheduledFlightRepository, never()).save(any());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void ensureExceptionThrownWhenAircraftRangeInsufficient() {
        OffsetDateTime departure = OffsetDateTime.now().plusDays(1);
        OffsetDateTime arrival = departure.plusHours(2);
        ScheduleFlightRequest request = new ScheduleFlightRequest("CS-TPA", "OPO", "LIS", departure, arrival);

        Route route = mock(Route.class);
        when(route.getOrigin()).thenReturn(new IataCode("OPO"));
        when(route.getDestination()).thenReturn(new IataCode("LIS"));
        when(route.getMinimumCapacity()).thenReturn(100);
        when(route.getMinimumRange()).thenReturn(5000.0);

        Aircraft aircraft = mock(Aircraft.class);
        when(aircraft.getRegistrationNumber()).thenReturn(new RegistrationNumber("CS-TPA"));
        when(aircraft.getStatus()).thenReturn(aisafe.aircrafts.domain.AircraftStatus.AVAILABLE);
        when(aircraft.getSeatCapacity()).thenReturn(200);
        when(aircraft.getRange()).thenReturn(2000.0);

        when(routeRepository.findByOriginAndDestination(any(), any())).thenReturn(Optional.of(route));
        when(aircraftRepository.findByRegistrationNumber(any())).thenReturn(Optional.of(aircraft));

        assertThrows(aisafe.flights.domain.AircraftIncompatibilityException.class, () -> useCase.execute(request));
        verify(scheduledFlightRepository, never()).save(any());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void ensureExceptionThrownWhenRouteDistanceExceedsRange() {
        OffsetDateTime departure = OffsetDateTime.now().plusDays(1);
        OffsetDateTime arrival = departure.plusHours(2);
        ScheduleFlightRequest request = new ScheduleFlightRequest("CS-TPA", "OPO", "LIS", departure, arrival);

        Route route = mock(Route.class);
        when(route.getOrigin()).thenReturn(new IataCode("OPO"));
        when(route.getDestination()).thenReturn(new IataCode("LIS"));
        when(route.getMinimumCapacity()).thenReturn(100);
        when(route.getMinimumRange()).thenReturn(1000.0);

        Aircraft aircraft = mock(Aircraft.class);
        when(aircraft.getRegistrationNumber()).thenReturn(new RegistrationNumber("CS-TPA"));
        when(aircraft.getStatus()).thenReturn(aisafe.aircrafts.domain.AircraftStatus.AVAILABLE);
        when(aircraft.getSeatCapacity()).thenReturn(200);
        when(aircraft.getRange()).thenReturn(2000.0);

        when(routeRepository.findByOriginAndDestination(any(), any())).thenReturn(Optional.of(route));
        when(aircraftRepository.findByRegistrationNumber(any())).thenReturn(Optional.of(aircraft));
        when(routeDistanceService.calculateDistanceKm(route)).thenReturn(3000.0); // > range (2000)

        assertThrows(aisafe.flights.domain.AircraftIncompatibilityException.class, () -> useCase.execute(request));
        verify(scheduledFlightRepository, never()).save(any());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void ensureExceptionThrownWhenAircraftScheduleOverlaps() {
        OffsetDateTime departure = OffsetDateTime.now().plusDays(1);
        OffsetDateTime arrival = departure.plusHours(2);
        ScheduleFlightRequest request = new ScheduleFlightRequest("CS-TPA", "OPO", "LIS", departure, arrival);

        Route route = mock(Route.class);
        when(route.getOrigin()).thenReturn(new IataCode("OPO"));
        when(route.getDestination()).thenReturn(new IataCode("LIS"));
        when(route.getMinimumCapacity()).thenReturn(100);
        when(route.getMinimumRange()).thenReturn(1000.0);

        Aircraft aircraft = mock(Aircraft.class);
        when(aircraft.getRegistrationNumber()).thenReturn(new RegistrationNumber("CS-TPA"));
        when(aircraft.getStatus()).thenReturn(aisafe.aircrafts.domain.AircraftStatus.AVAILABLE);
        when(aircraft.getSeatCapacity()).thenReturn(200);
        when(aircraft.getRange()).thenReturn(2000.0);

        when(routeRepository.findByOriginAndDestination(any(), any())).thenReturn(Optional.of(route));
        when(aircraftRepository.findByRegistrationNumber(any())).thenReturn(Optional.of(aircraft));
        when(routeDistanceService.calculateDistanceKm(route)).thenReturn(300.0);
        when(scheduledFlightRepository.existsByOverlappingSchedule(any(), any(), any())).thenReturn(true);

        assertThrows(aisafe.flights.domain.AircraftUnavailableException.class, () -> useCase.execute(request));
        verify(scheduledFlightRepository, never()).save(any());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void ensureExceptionThrownWhenRouteNotFound() {
        ScheduleFlightRequest request = new ScheduleFlightRequest("CS-TPA", "OPO", "LIS", OffsetDateTime.now(), OffsetDateTime.now().plusHours(2));

        when(routeRepository.findByOriginAndDestination(any(), any())).thenReturn(Optional.empty());

        assertThrows(aisafe.routes.domain.RouteNotFoundException.class, () -> useCase.execute(request));
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void ensureExceptionThrownWhenAircraftNotFound() {
        ScheduleFlightRequest request = new ScheduleFlightRequest("CS-TPA", "OPO", "LIS", OffsetDateTime.now(), OffsetDateTime.now().plusHours(2));

        Route route = mock(Route.class);
        when(routeRepository.findByOriginAndDestination(any(), any())).thenReturn(Optional.of(route));
        when(aircraftRepository.findByRegistrationNumber(any())).thenReturn(Optional.empty());

        assertThrows(aisafe.aircrafts.domain.AircraftNotFoundException.class, () -> useCase.execute(request));
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void ensureExceptionThrownWhenAircraftInactive() {
        OffsetDateTime departure = OffsetDateTime.now().plusDays(1);
        OffsetDateTime arrival = departure.plusHours(2);
        ScheduleFlightRequest request = new ScheduleFlightRequest("CS-TPA", "OPO", "LIS", departure, arrival);

        Route route = mock(Route.class);
        when(route.getOrigin()).thenReturn(new IataCode("OPO"));
        when(route.getDestination()).thenReturn(new IataCode("LIS"));

        Aircraft aircraft = mock(Aircraft.class);
        when(aircraft.getRegistrationNumber()).thenReturn(new RegistrationNumber("CS-TPA"));
        when(aircraft.getStatus()).thenReturn(aisafe.aircrafts.domain.AircraftStatus.INACTIVE);

        when(routeRepository.findByOriginAndDestination(any(), any())).thenReturn(Optional.of(route));
        when(aircraftRepository.findByRegistrationNumber(any())).thenReturn(Optional.of(aircraft));

        assertThrows(aisafe.flights.domain.AircraftUnavailableException.class, () -> useCase.execute(request));
    }
}
