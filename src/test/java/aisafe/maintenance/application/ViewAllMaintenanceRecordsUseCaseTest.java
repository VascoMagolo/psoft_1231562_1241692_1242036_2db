package aisafe.maintenance.application;

import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.maintenance.domain.MaintenanceRecordRepository;
import aisafe.shared.domain.PaginatedResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import aisafe.maintenance.domain.*;
import aisafe.aircrafts.domain.ModelName;
import aisafe.maintenance.application.dtos.ViewAllMaintenanceRecordsResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViewAllMaintenanceRecordsUseCaseTest {

    @Mock
    private MaintenanceRecordRepository repository;

    @Mock
    private AircraftRepository aircraftRepository;

    @InjectMocks
    private ViewAllMaintenanceRecordsUseCase viewAllMaintenanceRecords;

    @Test
    void ensureRecordsAreReturnedSuccessfully() {
        RegistrationNumber reg = new RegistrationNumber("CS-TPA");
        MaintenanceTemplate template = new MaintenanceTemplate("Annual Check", MaintenanceType.INSPECTION, List.of(new ModelName("B737")), List.of("Check"), 100, 30);
        MaintenancePart part = new MaintenancePart("P123", "Part 1", "Desc", 10, 5, MaintenanceComponent.ENGINE);
        MaintenanceRecord record = new MaintenanceRecord(
                UUID.randomUUID(), "R1", LocalDateTime.now().minusDays(5), 4,
                List.of(part), "Notes", template, MaintenanceStatus.COMPLETED,
                Set.of(MaintenanceComponent.ENGINE), reg, BigDecimal.valueOf(100), LocalDateTime.now().minusDays(5)
        );

        when(aircraftRepository.findByRegistrationNumber(reg)).thenReturn(Optional.of(mock(Aircraft.class)));
        when(repository.findByAircraftRegistration(eq(reg), anyInt(), anyInt())).thenReturn(new PaginatedResult<>(List.of(record), 1));

        PaginatedResult<ViewAllMaintenanceRecordsResponse> result = viewAllMaintenanceRecords.execute(reg, 0, 20);
        
        assertNotNull(result);
        assertEquals(1, result.data().size());
        assertEquals("CS-TPA", result.data().get(0).number());
        assertEquals("Annual Check", result.data().get(0).name());
        assertEquals(4, result.data().get(0).expectedDuration());
        assertEquals(MaintenanceStatus.COMPLETED, result.data().get(0).status());
        assertEquals("Notes", result.data().get(0).notes());
        assertEquals(List.of("P123"), result.data().get(0).partNumbers());
        assertEquals(Set.of("ENGINE"), result.data().get(0).components());

        // Cover equals/hashCode/toString of response record
        ViewAllMaintenanceRecordsResponse resp = result.data().get(0);
        assertNotNull(resp.toString());
        assertEquals(resp, new ViewAllMaintenanceRecordsResponse(List.of("P123"), "Annual Check", resp.startDate(), 4, MaintenanceStatus.COMPLETED, "Notes", "CS-TPA", Set.of("ENGINE")));
        assertEquals(resp.hashCode(), new ViewAllMaintenanceRecordsResponse(List.of("P123"), "Annual Check", resp.startDate(), 4, MaintenanceStatus.COMPLETED, "Notes", "CS-TPA", Set.of("ENGINE")).hashCode());
    }

    @Test
    void ensureExceptionWhenAircraftNotFound() {
        RegistrationNumber reg = new RegistrationNumber("CS-XXX");
        when(aircraftRepository.findByRegistrationNumber(reg)).thenReturn(Optional.empty());

        assertThrows(AircraftNotFoundException.class, () ->
                viewAllMaintenanceRecords.execute(reg, 0, 20));
        verify(repository, never()).findByAircraftRegistration(any(RegistrationNumber.class), anyInt(), anyInt());
    }
}
