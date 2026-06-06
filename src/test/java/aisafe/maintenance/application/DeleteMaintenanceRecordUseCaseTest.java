package aisafe.maintenance.application;

import aisafe.maintenance.domain.MaintenanceComponent;
import aisafe.maintenance.domain.MaintenancePart;
import aisafe.maintenance.domain.MaintenanceRecord;
import aisafe.maintenance.domain.MaintenanceRecordNotFoundException;
import aisafe.maintenance.domain.MaintenanceRecordRepository;
import aisafe.maintenance.domain.MaintenanceStatus;
import aisafe.maintenance.domain.MaintenanceTemplate;
import aisafe.maintenance.domain.MaintenanceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteMaintenanceRecordUseCaseTest {

    @Mock
    private MaintenanceRecordRepository maintenanceRecordRepository;

    @InjectMocks
    private DeleteMaintenanceRecordUseCase deleteMaintenanceRecord;

    private MaintenanceRecord buildRecord() {
        MaintenancePart part = new MaintenancePart("PN-001", "Engine Filter", "A filter", 10, 2, MaintenanceComponent.ENGINE);
        MaintenanceTemplate template = new MaintenanceTemplate("Engine Check", MaintenanceType.INSPECTION,
                List.of("ModelA"), List.of("Check oil"), 500, 90);
        return new MaintenanceRecord("Oil change", LocalDateTime.now(), 2, part, "notes", template,
                MaintenanceStatus.PLANNED, "CS-ABD");
    }

    @Test
    void ensureMaintenanceRecordIsDeletedSuccessfully() {
        MaintenanceRecord record = buildRecord();
        when(maintenanceRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        assertDoesNotThrow(() -> deleteMaintenanceRecord.execute(1L));
        verify(maintenanceRecordRepository).delete(any(MaintenanceRecord.class));
    }

    @Test
    void ensureExceptionWhenMaintenanceRecordNotFound() {
        when(maintenanceRecordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MaintenanceRecordNotFoundException.class, () -> deleteMaintenanceRecord.execute(99L));
        verify(maintenanceRecordRepository, never()).delete(any());
    }
}
