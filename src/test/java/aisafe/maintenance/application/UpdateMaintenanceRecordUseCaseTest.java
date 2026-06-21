package aisafe.maintenance.application;

import aisafe.aircrafts.domain.ModelName;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.maintenance.application.dtos.MaintenanceRecordResponse;
import aisafe.maintenance.application.dtos.UpdateMaintenanceRecordsRequest;
import aisafe.maintenance.domain.*;
import aisafe.shared.domain.ConcurrencyException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
        return new MaintenanceTemplate("Annual Check", MaintenanceType.INSPECTION, List.of(new ModelName("A320")), List.of("Check engine"), 500, 365);
    }

    @Test
    void ensureRecordIsUpdatedSuccessfully() {
        UpdateMaintenanceRecordsRequest request = new UpdateMaintenanceRecordsRequest(MaintenanceStatus.IN_PROGRESS, "Updated notes");

        MaintenanceRecord record = new MaintenanceRecord(
                UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4, List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA"), BigDecimal.valueOf(1000), null);

        when(recordRepository.findByRecordId(any(UUID.class))).thenReturn(Optional.of(record));
        when(recordRepository.findVersionFor(any(UUID.class))).thenReturn(0L).thenReturn(1L);
        when(recordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

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

        MaintenanceRecord record = new MaintenanceRecord(
                UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4, List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA"), BigDecimal.valueOf(1000), null);

        when(recordRepository.findByRecordId(any(UUID.class))).thenReturn(Optional.of(record));
        when(recordRepository.findVersionFor(any(UUID.class))).thenReturn(1L); // Database has version 1

        assertThrows(ConcurrencyException.class, () ->
                updateMaintenanceRecord.execute(record.getRecordId(), request, 0L)); // Client provides version 0
        verify(recordRepository, never()).save(any());
    }

    @Test
    void ensureRecordIsUpdatedSuccessfullyWithNullNotes() {
        UpdateMaintenanceRecordsRequest request = new UpdateMaintenanceRecordsRequest(MaintenanceStatus.IN_PROGRESS, null);

        MaintenanceRecord record = new MaintenanceRecord(
                UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4, List.of(buildPart()), "Original notes", buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA"), BigDecimal.valueOf(1000), null);

        when(recordRepository.findByRecordId(any(UUID.class))).thenReturn(Optional.of(record));
        when(recordRepository.findVersionFor(any(UUID.class))).thenReturn(0L).thenReturn(1L);
        when(recordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MaintenanceRecordResponse response = updateMaintenanceRecord.execute(record.getRecordId(), request, 0L);

        assertNotNull(response);
        assertEquals("Original notes", record.getNotes());
    }

    @Test
    void ensureRecordIsUpdatedSuccessfullyWithEmptyNotes() {
        UpdateMaintenanceRecordsRequest request = new UpdateMaintenanceRecordsRequest(MaintenanceStatus.IN_PROGRESS, "   ");

        MaintenanceRecord record = new MaintenanceRecord(
                UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4, List.of(buildPart()), "Original notes", buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA"), BigDecimal.valueOf(1000), null);

        when(recordRepository.findByRecordId(any(UUID.class))).thenReturn(Optional.of(record));
        when(recordRepository.findVersionFor(any(UUID.class))).thenReturn(0L).thenReturn(1L);
        when(recordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MaintenanceRecordResponse response = updateMaintenanceRecord.execute(record.getRecordId(), request, 0L);

        assertNotNull(response);
        assertEquals("Original notes", record.getNotes());
    }
}
