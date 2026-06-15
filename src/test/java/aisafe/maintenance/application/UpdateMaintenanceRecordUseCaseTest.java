package aisafe.maintenance.application;

import aisafe.maintenance.application.dtos.MaintenanceRecordResponse;
import aisafe.maintenance.application.dtos.UpdateMaintenanceRecordsRequest;
import aisafe.maintenance.domain.*;
import aisafe.shared.domain.ConcurrencyException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateMaintenanceRecordUseCaseTest {

    @Mock
    private MaintenanceRecordRepository recordRepository;

    @InjectMocks
    private UpdateMaintenanceRecordUseCase updateMaintenanceRecord;

    private MaintenancePart buildPart() {
        return new MaintenancePart("P001", "Engine Filter", null, 10, 2, MaintenanceComponent.ENGINE);
    }

    private MaintenanceTemplate buildTemplate() {
        return new MaintenanceTemplate("Annual Check", MaintenanceType.INSPECTION, List.of("A320"), List.of("Check engine"), 500, 365);
    }

    @Test
    void ensureRecordIsUpdatedSuccessfully() {
        UpdateMaintenanceRecordsRequest request = new UpdateMaintenanceRecordsRequest(MaintenanceStatus.IN_PROGRESS, "Updated notes");

        MaintenanceRecord record = spy(new MaintenanceRecord(
                UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4, List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, "CS-TPA"));
        doReturn(0L).when(record).getVersion();

        when(recordRepository.findByRecordId(any(UUID.class))).thenReturn(Optional.of(record));
        doNothing().when(recordRepository).save(any());

        UUID recordId = record.getRecordId();
        MaintenanceRecordResponse response = updateMaintenanceRecord.execute(recordId, request, 0L);

        assertNotNull(response);
        verify(recordRepository).save(record);
    }

    @Test
    void ensureExceptionWhenStatusIsNull() {
        UpdateMaintenanceRecordsRequest request = new UpdateMaintenanceRecordsRequest(null, "notes");

        assertThrows(MaintenanceInvalidFieldException.class, () ->
                updateMaintenanceRecord.execute(UUID.randomUUID(), request, 0L));
        verify(recordRepository, never()).findByRecordId(any());
    }

    @Test
    void ensureExceptionWhenRecordNotFound() {
        UpdateMaintenanceRecordsRequest request = new UpdateMaintenanceRecordsRequest(MaintenanceStatus.IN_PROGRESS, null);

        when(recordRepository.findByRecordId(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(MaintenanceRecordNotFoundException.class, () ->
                updateMaintenanceRecord.execute(UUID.randomUUID(), request, 0L));
        verify(recordRepository, never()).save(any());
    }

    @Test
    void ensureExceptionWhenVersionMismatch() {
        UpdateMaintenanceRecordsRequest request = new UpdateMaintenanceRecordsRequest(MaintenanceStatus.IN_PROGRESS, null);

        MaintenanceRecord record = spy(new MaintenanceRecord(
                UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4, List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, "CS-TPA"));
        doReturn(1L).when(record).getVersion();

        when(recordRepository.findByRecordId(any(UUID.class))).thenReturn(Optional.of(record));

        assertThrows(ConcurrencyException.class, () ->
                updateMaintenanceRecord.execute(record.getRecordId(), request, 0L));
        verify(recordRepository, never()).save(any());
    }
}
