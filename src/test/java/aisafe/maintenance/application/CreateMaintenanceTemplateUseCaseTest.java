package aisafe.maintenance.application;

import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelRepository;
import aisafe.aircrafts.domain.Manufacturer;
import aisafe.maintenance.application.dtos.CreateMaintenanceTemplateRequest;
import aisafe.maintenance.application.dtos.MaintenanceTemplateResponse;
import aisafe.maintenance.domain.MaintenanceTemplate;
import aisafe.maintenance.domain.MaintenanceTemplateRepository;
import aisafe.maintenance.domain.MaintenanceType;
import aisafe.shared.domain.DuplicateResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateMaintenanceTemplateUseCaseTest {

    @Mock
    private MaintenanceTemplateRepository maintenanceTemplateRepository;

    @Mock
    private AircraftModelRepository modelRepository;

    @InjectMocks
    private CreateMaintenanceTemplateUseCase createMaintenanceTemplate;

    private CreateMaintenanceTemplateRequest buildRequest() {
        return new CreateMaintenanceTemplateRequest("100h Check", MaintenanceType.INSPECTION, List.of("A320"), List.of("Check oil"), 100, 30);
    }

    @Test
    void ensureTemplateIsCreatedSuccessfully() {
        AircraftModel model = new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "a320.jpg", 180);
        when(modelRepository.findByModelName("A320")).thenReturn(Optional.of(model));
        when(maintenanceTemplateRepository.existsByName(anyString())).thenReturn(false);

        MaintenanceTemplateResponse response = createMaintenanceTemplate.execute(buildRequest());

        assertNotNull(response);
        assertEquals("100h Check", response.name());
        verify(maintenanceTemplateRepository, times(1)).save(any(MaintenanceTemplate.class));
    }

    @Test
    void ensureExceptionWhenTemplateNameAlreadyExists() {
        AircraftModel model = new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "a320.jpg", 180);
        when(modelRepository.findByModelName("A320")).thenReturn(Optional.of(model));
        when(maintenanceTemplateRepository.existsByName(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> createMaintenanceTemplate.execute(buildRequest()));
        verify(maintenanceTemplateRepository, never()).save(any());
    }
}
