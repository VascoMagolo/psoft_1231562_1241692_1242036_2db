package aisafe.maintenance.application;

import aisafe.maintenance.domain.MaintenanceRecord;
import aisafe.maintenance.domain.MaintenanceRecordRepository;
import aisafe.maintenance.domain.MaintenanceStatus;
import aisafe.shared.domain.PaginatedResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewOngoingMaintenanceUseCaseTest {

    @Mock
    private MaintenanceRecordRepository repository;

    @InjectMocks
    private ViewOngoingMaintenanceUseCase viewOngoingMaintenance;

    @Test
    void ensureReturnsEmptyPageWhenNoOngoingRecords() {
        when(repository.findByStatus(eq(MaintenanceStatus.IN_PROGRESS), anyInt(), anyInt()))
                .thenReturn(new PaginatedResult<>(List.of(), 0));

        PaginatedResult<?> result = viewOngoingMaintenance.execute(0, 20);

        assertEquals(0, result.totalElements());
        assertEquals(0, result.data().size());
    }

    @Test
    void ensureReturnsOngoingRecords() {
        MaintenanceRecord record = mock(MaintenanceRecord.class);
        when(record.getRecordId()).thenReturn(java.util.UUID.randomUUID());
        when(record.getDescription()).thenReturn("Engine overhaul");
        when(record.getStartDate()).thenReturn(java.time.LocalDateTime.of(2026, 6, 1, 8, 0));
        when(record.getExpectedDuration()).thenReturn(8);
        when(record.getNotes()).thenReturn(null);
        when(record.getParts()).thenReturn(List.of());
        when(record.getTemplate()).thenReturn(mock(aisafe.maintenance.domain.MaintenanceTemplate.class));
        when(record.getTemplate().getName()).thenReturn("Annual Check");
        when(record.getStatus()).thenReturn(MaintenanceStatus.IN_PROGRESS);
        when(record.getAircraftRegistration()).thenReturn(new aisafe.aircrafts.domain.RegistrationNumber("CS-TPA"));
        when(record.getComponents()).thenReturn(java.util.Set.of(aisafe.maintenance.domain.MaintenanceComponent.ENGINE));
        when(repository.findByStatus(eq(MaintenanceStatus.IN_PROGRESS), anyInt(), anyInt()))
                .thenReturn(new PaginatedResult<>(List.of(record), 1));
        when(repository.findVersionFor(record.getRecordId())).thenReturn(0L);

        PaginatedResult<?> result = viewOngoingMaintenance.execute(0, 20);

        assertEquals(1, result.totalElements());
        assertEquals(1, result.data().size());
    }
}
