package aisafe.maintenance.application;

import aisafe.maintenance.application.dtos.MaintenancePartResponse;
import aisafe.maintenance.application.dtos.UpdateMaintenancePartRequest;
import aisafe.maintenance.domain.MaintenanceComponent;
import aisafe.maintenance.domain.MaintenancePart;
import aisafe.maintenance.domain.MaintenancePartNotFoundException;
import aisafe.maintenance.domain.MaintenancePartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateMaintenancePartUseCaseTest {

    @Mock
    private MaintenancePartRepository repository;

    @InjectMocks
    private UpdateMaintenancePartUseCase useCase;

    private MaintenancePart part;

    @BeforeEach
    void setUp() {
        part = new MaintenancePart("P001", "Engine Filter", "Old description", 10, 5, MaintenanceComponent.ENGINE);
    }

    @Test
    void ensureUpdatesPartSuccessfully() {
        when(repository.findByPartNumber("P001")).thenReturn(Optional.of(part));
        UpdateMaintenancePartRequest request = new UpdateMaintenancePartRequest("New description", 20, 10);

        MaintenancePartResponse response = useCase.execute("P001", request);

        assertNotNull(response);
        assertEquals("P001", response.partNumber());
        assertEquals("New description", part.getDescription());
        assertEquals(20, part.getStockQuantity());
        assertEquals(10, part.getMinimumThreshold());
        verify(repository).save(part);
    }

    @Test
    void ensureThrowsExceptionWhenPartNotFound() {
        when(repository.findByPartNumber("P001")).thenReturn(Optional.empty());
        UpdateMaintenancePartRequest request = new UpdateMaintenancePartRequest("New description", 20, 10);

        assertThrows(MaintenancePartNotFoundException.class, () -> useCase.execute("P001", request));
        verify(repository, never()).save(any());
    }
}
