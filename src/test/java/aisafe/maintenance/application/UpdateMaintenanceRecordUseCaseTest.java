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

import java.time.LocalDate;
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
        AircraftModel model = new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "a320.jpg", 180);
        return new MaintenanceTemplate("Annual Check", MaintenanceType.INSPECTION, List.of(model), List.of("Check engine"), 500, 365);
    }

    private Aircraft buildAircraft() {
        AircraftModel model = new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "a320.jpg", 180);
        return new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1), model, new RegistrationNumber("CS-TPA"), 150, List.of());
    }

    @Test
    void ensureRecordIsUpdatedSuccessfully() {
        UpdateMaintenanceRecordsRequest request = new UpdateMaintenanceRecordsRequest(MaintenanceStatus.IN_PROGRESS, "Updated notes");

        MaintenanceRecord record = spy(new MaintenanceRecord(
                "Engine check", LocalDateTime.now(), 4, buildPart(), null, buildTemplate(), MaintenanceStatus.PLANNED, buildAircraft()));
        doReturn(0L).when(record).getVersion();

        when(recordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(recordRepository.save(any())).thenReturn(record);

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
                "Engine check", LocalDateTime.now(), 4, buildPart(), null, buildTemplate(), MaintenanceStatus.PLANNED, buildAircraft()));
        doReturn(1L).when(record).getVersion();

        when(recordRepository.findById(1L)).thenReturn(Optional.of(record));

        assertThrows(ObjectOptimisticLockingFailureException.class, () ->
                updateMaintenanceRecord.execute(1L, request, 0L));
        verify(recordRepository, never()).save(any());
    }
}
