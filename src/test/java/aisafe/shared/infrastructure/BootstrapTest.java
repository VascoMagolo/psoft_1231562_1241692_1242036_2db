package aisafe.shared.infrastructure;

import aisafe.aircrafts.domain.AircraftModelRepository;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.airports.domain.AirportRepository;
import aisafe.maintenance.domain.MaintenancePartRepository;
import aisafe.maintenance.domain.MaintenanceRecordRepository;
import aisafe.maintenance.domain.MaintenanceTemplateRepository;
import aisafe.routes.domain.RouteRepository;
import aisafe.flights.domain.ScheduledFlightRepository;
import aisafe.security.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BootstrapTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AircraftModelRepository aircraftModelRepository;
    @Mock
    private AircraftRepository aircraftRepository;
    @Mock
    private AirportRepository airportRepository;
    @Mock
    private MaintenancePartRepository maintenancePartRepository;
    @Mock
    private MaintenanceTemplateRepository maintenanceTemplateRepository;
    @Mock
    private MaintenanceRecordRepository maintenanceRecordRepository;
    @Mock
    private RouteRepository routeRepository;
    @Mock
    private ScheduledFlightRepository scheduledFlightRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private Bootstrap bootstrap;

    @Test
    void ensureNoActionWhenAllCountsNonZero() {
        when(userRepository.count()).thenReturn(1L);
        when(aircraftModelRepository.count()).thenReturn(1L);
        when(aircraftRepository.count()).thenReturn(1L);
        when(airportRepository.count()).thenReturn(1L);
        when(maintenancePartRepository.count()).thenReturn(1L);
        when(maintenanceTemplateRepository.count()).thenReturn(1L);
        when(maintenanceRecordRepository.count()).thenReturn(1L);
        when(routeRepository.count()).thenReturn(1L);

        ApplicationArguments args = mock(ApplicationArguments.class);
        bootstrap.run(args);

        // Verify no saves were called
        verify(userRepository, never()).save(any());
        verify(aircraftModelRepository, never()).save(any());
        verify(aircraftRepository, never()).save(any());
        verify(airportRepository, never()).save(any());
        verify(maintenancePartRepository, never()).save(any());
        verify(maintenanceTemplateRepository, never()).save(any());
        verify(maintenanceRecordRepository, never()).save(any());
        verify(routeRepository, never()).save(any());
    }

    @Test
    void ensureScheduledFlightsNotBootstrappedWhenCountNonZero() {
        when(userRepository.count()).thenReturn(1L);
        when(aircraftModelRepository.count()).thenReturn(1L);
        when(aircraftRepository.count()).thenReturn(1L);
        when(airportRepository.count()).thenReturn(1L);
        when(maintenancePartRepository.count()).thenReturn(1L);
        when(maintenanceTemplateRepository.count()).thenReturn(1L);
        when(maintenanceRecordRepository.count()).thenReturn(1L);
        when(routeRepository.count()).thenReturn(0L); // Triggers route bootstrap
        when(scheduledFlightRepository.count()).thenReturn(1L); // Skips flight bootstrap

        ApplicationArguments args = mock(ApplicationArguments.class);
        bootstrap.run(args);

        verify(routeRepository, atLeastOnce()).save(any());
        verify(scheduledFlightRepository, never()).save(any());
    }
}
