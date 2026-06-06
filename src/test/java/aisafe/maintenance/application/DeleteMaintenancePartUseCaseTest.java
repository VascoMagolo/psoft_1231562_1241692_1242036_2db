package aisafe.maintenance.application;

import aisafe.maintenance.domain.MaintenanceComponent;
import aisafe.maintenance.domain.MaintenancePart;
import aisafe.maintenance.domain.MaintenancePartNotFoundException;
import aisafe.maintenance.domain.MaintenancePartRepository;
import aisafe.maintenance.domain.MaintenanceRecordRepository;
import aisafe.shared.domain.ResourceInUseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteMaintenancePartUseCaseTest {

    @Mock
    private MaintenancePartRepository maintenancePartRepository;

    @Mock
    private MaintenanceRecordRepository maintenanceRecordRepository;

    @InjectMocks
    private DeleteMaintenancePartUseCase deleteMaintenancePart;

    private MaintenancePart buildPart() {
        return new MaintenancePart("PN-001", "Engine Filter", "A filter", 10, 2, MaintenanceComponent.ENGINE);
    }

    @Test
    void ensureMaintenancePartIsDeletedSuccessfully() {
        MaintenancePart part = buildPart();
        when(maintenancePartRepository.findById(1L)).thenReturn(Optional.of(part));
        when(maintenanceRecordRepository.existsByPart(part)).thenReturn(false);

        assertDoesNotThrow(() -> deleteMaintenancePart.execute(1L));
        verify(maintenancePartRepository).delete(any(MaintenancePart.class));
    }

    @Test
    void ensureExceptionWhenMaintenancePartNotFound() {
        when(maintenancePartRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MaintenancePartNotFoundException.class, () -> deleteMaintenancePart.execute(99L));
        verify(maintenancePartRepository, never()).delete(any());
    }

    @Test
    void ensureExceptionWhenMaintenancePartIsInUse() {
        MaintenancePart part = buildPart();
        when(maintenancePartRepository.findById(1L)).thenReturn(Optional.of(part));
        when(maintenanceRecordRepository.existsByPart(part)).thenReturn(true);

        assertThrows(ResourceInUseException.class, () -> deleteMaintenancePart.execute(1L));
        verify(maintenancePartRepository, never()).delete(any());
    }
}
