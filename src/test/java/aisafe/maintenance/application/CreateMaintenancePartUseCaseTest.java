package aisafe.maintenance.application;

import aisafe.maintenance.application.dtos.CreateMaintenancePartRequest;
import aisafe.maintenance.application.dtos.MaintenancePartResponse;
import aisafe.maintenance.domain.MaintenancePart;
import aisafe.maintenance.domain.MaintenancePartRepository;
import aisafe.maintenance.domain.MaintenanceComponent;
import aisafe.shared.domain.DuplicateResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateMaintenancePartUseCaseTest {

    @Mock
    private MaintenancePartRepository maintenancePartRepository;

    @InjectMocks
    private CreateMaintenancePartUseCase createMaintenancePart;

    private CreateMaintenancePartRequest buildRequest() {
        return new CreateMaintenancePartRequest("ENG-001", "Engine Filter", "High-efficiency filter", 100, 10, MaintenanceComponent.ENGINE);
    }

    @Test
    void ensurePartIsCreatedSuccessfully() {
        when(maintenancePartRepository.existsByPartNumber(anyString())).thenReturn(false);

        MaintenancePartResponse response = createMaintenancePart.execute(buildRequest());

        assertNotNull(response);
        assertEquals("ENG-001", response.partNumber());
        verify(maintenancePartRepository, times(1)).save(any(MaintenancePart.class));
    }

    @Test
    void ensureExceptionWhenPartNumberAlreadyExists() {
        when(maintenancePartRepository.existsByPartNumber(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> createMaintenancePart.execute(buildRequest()));
        verify(maintenancePartRepository, never()).save(any());
    }
}
