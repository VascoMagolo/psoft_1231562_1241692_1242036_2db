package aisafe.maintenance.application;

import aisafe.aircrafts.domain.*;
import aisafe.flights.domain.ScheduledFlightRepository;
import aisafe.maintenance.application.dtos.MaintenanceDueAircraftResponse;
import aisafe.maintenance.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViewMaintenanceDueAircraftUseCaseTest {

    @Mock
    private AircraftRepository aircraftRepository;

    @Mock
    private MaintenanceRecordRepository maintenanceRecordRepository;

    @Mock
    private MaintenanceTemplateRepository maintenanceTemplateRepository;

    @Mock
    private ScheduledFlightRepository flightRepository;

    private ViewMaintenanceDueAircraftUseCase useCase;

    private Aircraft aircraft;
    private AircraftModel model;
    private MaintenanceTemplate template;

    @BeforeEach
    void setUp() {
        useCase = new ViewMaintenanceDueAircraftUseCase(
                aircraftRepository,
                maintenanceRecordRepository,
                maintenanceTemplateRepository,
                flightRepository,
                300.0,
                365L
        );

        model = new AircraftModel("B737", Manufacturer.BOEING, 20000.0, 5000.0, 800.0, "img.png", 180);
        aircraft = new Aircraft(AircraftStatus.AVAILABLE, LocalDate.now().minusDays(40), model, new RegistrationNumber("CS-TKA"), 180, 5000.0, List.of("WiFi"));
        template = new MaintenanceTemplate("Annual Check", MaintenanceType.INSPECTION, List.of("B737"), List.of("Check"), 100, 30);
    }

    @Test
    void ensureAircraftNotDueReturnsEmptyList() {
        // Set manufacturing date to 10 days ago (below 30 days) and flight hours to 20.0 (below 100 hrs)
        Aircraft recentAircraft = new Aircraft(AircraftStatus.AVAILABLE, LocalDate.now().minusDays(10), model, new RegistrationNumber("CS-TKA"), 180, 5000.0, List.of("WiFi"));

        when(aircraftRepository.findAll()).thenReturn(List.of(recentAircraft));
        when(maintenanceTemplateRepository.findAll()).thenReturn(List.of(template));
        when(maintenanceRecordRepository.findCompletedByAircraft(any())).thenReturn(Collections.emptyList());
        when(flightRepository.calculateOperationalHoursSince(any(), any())).thenReturn(20.0);

        List<MaintenanceDueAircraftResponse> result = useCase.execute();
        assertTrue(result.isEmpty());
    }

    @Test
    void ensureAircraftDueForFlightHoursIsReturned() {
        when(aircraftRepository.findAll()).thenReturn(List.of(aircraft));
        when(maintenanceTemplateRepository.findAll()).thenReturn(List.of(template));
        when(maintenanceRecordRepository.findCompletedByAircraft(any())).thenReturn(Collections.emptyList());
        // Flight hours exceeds limit (120 >= 100)
        when(flightRepository.calculateOperationalHoursSince(any(), any())).thenReturn(120.0);

        List<MaintenanceDueAircraftResponse> result = useCase.execute();
        assertEquals(1, result.size());
        assertEquals("CS-TKA", result.get(0).registrationNumber());
        assertTrue(result.get(0).dueReason().contains("Exceeded flight hours limit"));
    }

    @Test
    void ensureAircraftDueForCalendarDaysIsReturned() {
        // Manufacturing date is 40 days ago (exceeds template limit of 30 days)
        when(aircraftRepository.findAll()).thenReturn(List.of(aircraft));
        when(maintenanceTemplateRepository.findAll()).thenReturn(List.of(template));
        when(maintenanceRecordRepository.findCompletedByAircraft(any())).thenReturn(Collections.emptyList());
        // Flight hours is below limit (10.0 < 100)
        when(flightRepository.calculateOperationalHoursSince(any(), any())).thenReturn(10.0);

        List<MaintenanceDueAircraftResponse> result = useCase.execute();
        assertEquals(1, result.size());
        assertEquals("CS-TKA", result.get(0).registrationNumber());
        assertTrue(result.get(0).dueReason().contains("Exceeded elapsed days limit"));
    }

    @Test
    void ensureCompletedMaintenanceResetsDueState() {
        // Last completed record was 5 days ago (less than 30)
        MaintenancePart part = new MaintenancePart("P123", "Part 1", "Desc", 10, 5, MaintenanceComponent.ENGINE);
        MaintenanceRecord record = new MaintenanceRecord(
                UUID.randomUUID(), "R1", LocalDateTime.now().minusDays(5), 4,
                List.of(part), "Notes", template, MaintenanceStatus.COMPLETED,
                Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TKA"), BigDecimal.valueOf(100), LocalDateTime.now().minusDays(5)
        );

        when(aircraftRepository.findAll()).thenReturn(List.of(aircraft));
        when(maintenanceTemplateRepository.findAll()).thenReturn(List.of(template));
        when(maintenanceRecordRepository.findCompletedByAircraft(any())).thenReturn(List.of(record));
        // Flight hours since last completed was 10.0 (less than 100)
        when(flightRepository.calculateOperationalHoursSince(any(), any())).thenReturn(10.0);

        List<MaintenanceDueAircraftResponse> result = useCase.execute();
        assertTrue(result.isEmpty());
    }

    @Test
    void ensureFallbackToGlobalThresholdsWhenNoTemplatesApply() {
        // Template is for A320, so does not apply to our B737 aircraft
        MaintenanceTemplate diffTemplate = new MaintenanceTemplate("Annual Check", MaintenanceType.INSPECTION, List.of("A320"), List.of("Check"), 100, 30);

        when(aircraftRepository.findAll()).thenReturn(List.of(aircraft));
        when(maintenanceTemplateRepository.findAll()).thenReturn(List.of(diffTemplate));
        when(maintenanceRecordRepository.findCompletedByAircraft(any())).thenReturn(Collections.emptyList());
        // Global threshold is 300 hrs, we return 350.0 hrs
        when(flightRepository.calculateOperationalHoursSince(any(), any())).thenReturn(350.0);

        List<MaintenanceDueAircraftResponse> result = useCase.execute();
        assertEquals(1, result.size());
        assertEquals("CS-TKA", result.get(0).registrationNumber());
        assertTrue(result.get(0).dueReason().contains("Exceeded default flight hours limit"));
    }
}
