package aisafe.maintenance.application;

import aisafe.maintenance.application.dtos.CreateMaintenancePartRequest;
import aisafe.maintenance.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateMaintenancePartUseCaseTest {

    @Mock
    private MaintenancePartRepository maintenancePartRepository;

    @InjectMocks
    private CreateMaintenancePartUseCase createMaintenancePart;

    private CreateMaintenancePartRequest buildRequest() {
        return new CreateMaintenancePartRequest("P001", "Engine Filter", null, 10, 2, MaintenanceComponent.ENGINE);
    }

    @Test
    void ensurePartIsCreatedSuccessfully() {
        when(maintenancePartRepository.existsByPartNumber("P001")).thenReturn(false);
        doNothing().when(maintenancePartRepository).save(any());

        assertDoesNotThrow(() -> createMaintenancePart.execute(buildRequest()));
        verify(maintenancePartRepository).save(any(MaintenancePart.class));
    }

    @Test
    void ensureExceptionWhenPartNumberAlreadyExists() {
        when(maintenancePartRepository.existsByPartNumber("P001")).thenReturn(true);

        assertThrows(MaintenancePartAlreadyExistsException.class, () -> createMaintenancePart.execute(buildRequest()));
        verify(maintenancePartRepository, never()).save(any());
    }
}
