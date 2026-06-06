package aisafe.maintenance.application;

import aisafe.aircrafts.domain.*;
import aisafe.maintenance.application.dtos.MaintenanceRecordResponse;
import aisafe.maintenance.application.dtos.UpdateMaintenanceRecordsRequest;
import aisafe.maintenance.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
                "Engine check", LocalDateTime.now(), 4, buildPart(), null, buildTemplate(), MaintenanceStatus.PLANNED, "CS-TPA"));
        doReturn(0L).when(record).getVersion();

        when(recordRepository.findById(1L)).thenReturn(Optional.of(record));
        doNothing().when(recordRepository).save(any());

        MaintenanceRecordResponse response = updateMaintenanceRecord.execute(1L, request, 0L);

        assertNotNull(response);
        verify(recordRepository).save(record);
    }

    @Test
    void ensureExceptionWhenStatusIsNull() {
        UpdateMaintenanceRecordsRequest request = new UpdateMaintenanceRecordsRequest(null, "notes");

        assertThrows(MaintenanceInvalidFieldException.class, () ->
                updateMaintenanceRecord.execute(1L, request, 0L));
        verify(recordRepository, never()).findById(any());
    }

    @Test
    void ensureExceptionWhenRecordNotFound() {
        UpdateMaintenanceRecordsRequest request = new UpdateMaintenanceRecordsRequest(MaintenanceStatus.IN_PROGRESS, null);

        when(recordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MaintenanceRecordNotFoundException.class, () ->
                updateMaintenanceRecord.execute(99L, request, 0L));
        verify(recordRepository, never()).save(any());
    }

    @Test
    void ensureExceptionWhenVersionMismatch() {
        UpdateMaintenanceRecordsRequest request = new UpdateMaintenanceRecordsRequest(MaintenanceStatus.IN_PROGRESS, null);

        MaintenanceRecord record = spy(new MaintenanceRecord(
                "Engine check", LocalDateTime.now(), 4, buildPart(), null, buildTemplate(), MaintenanceStatus.PLANNED, "CS-TPA"));
        doReturn(1L).when(record).getVersion();

        when(recordRepository.findById(1L)).thenReturn(Optional.of(record));

        assertThrows(ObjectOptimisticLockingFailureException.class, () ->
                updateMaintenanceRecord.execute(1L, request, 0L));
        verify(recordRepository, never()).save(any());
    }
}
