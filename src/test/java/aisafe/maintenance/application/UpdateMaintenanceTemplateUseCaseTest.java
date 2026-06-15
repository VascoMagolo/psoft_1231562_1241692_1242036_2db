package aisafe.maintenance.application;

import aisafe.maintenance.application.dtos.MaintenanceTemplateResponse;
import aisafe.maintenance.application.dtos.UpdateMaintenanceTemplateRequest;
import aisafe.maintenance.domain.MaintenanceTemplate;
import aisafe.maintenance.domain.MaintenanceTemplateNotFoundException;
import aisafe.maintenance.domain.MaintenanceTemplateRepository;
import aisafe.maintenance.domain.MaintenanceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateMaintenanceTemplateUseCaseTest {

    @Mock
    private MaintenanceTemplateRepository repository;

    @InjectMocks
    private UpdateMaintenanceTemplateUseCase useCase;

    private MaintenanceTemplate template;

    @BeforeEach
    void setUp() {
        template = new MaintenanceTemplate("100H Check", MaintenanceType.INSPECTION, List.of("A320"), List.of("Check engine"), 100, 30);
    }

    @Test
    void ensureUpdatesTemplateSuccessfully() {
        when(repository.findByName("100H Check")).thenReturn(Optional.of(template));
        UpdateMaintenanceTemplateRequest request = new UpdateMaintenanceTemplateRequest(List.of("Check engine", "Check brakes"), 150, 45);

        MaintenanceTemplateResponse response = useCase.execute("100H Check", request);

        assertNotNull(response);
        assertEquals("100H Check", response.name());
        assertEquals(150, template.getIntervalFlightHours());
        assertEquals(45, template.getIntervalDays());
        assertEquals(2, template.getChecklist().size());
        verify(repository).save(template);
    }

    @Test
    void ensureThrowsExceptionWhenTemplateNotFound() {
        when(repository.findByName("100H Check")).thenReturn(Optional.empty());
        UpdateMaintenanceTemplateRequest request = new UpdateMaintenanceTemplateRequest(List.of("Check engine"), 150, 45);

        assertThrows(MaintenanceTemplateNotFoundException.class, () -> useCase.execute("100H Check", request));
        verify(repository, never()).save(any());
    }
}
