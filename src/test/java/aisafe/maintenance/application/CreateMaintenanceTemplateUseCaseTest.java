package aisafe.maintenance.application;

import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelNotFoundException;
import aisafe.aircrafts.domain.AircraftModelRepository;
import aisafe.aircrafts.domain.Manufacturer;
import aisafe.maintenance.application.dtos.CreateMaintenanceTemplateRequest;
import aisafe.maintenance.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateMaintenanceTemplateUseCaseTest {

    @Mock
    private MaintenanceTemplateRepository maintenanceTemplateRepository;

    @Mock
    private AircraftModelRepository modelRepository;

    @InjectMocks
    private CreateMaintenanceTemplateUseCase createMaintenanceTemplate;

    private AircraftModel buildModel() {
        return new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "a320.jpg", 180);
    }

    private CreateMaintenanceTemplateRequest buildRequest() {
        return new CreateMaintenanceTemplateRequest(
                "Annual Check", MaintenanceType.INSPECTION, List.of("A320"),
                List.of("Check engine", "Check fuel"), 500, 365);
    }

    @Test
    void ensureTemplateIsCreatedSuccessfully() {
        when(modelRepository.findByModelName("A320")).thenReturn(Optional.of(buildModel()));
        when(maintenanceTemplateRepository.existsByName("Annual Check")).thenReturn(false);
        doNothing().when(maintenanceTemplateRepository).save(any());

        assertDoesNotThrow(() -> createMaintenanceTemplate.execute(buildRequest()));
        verify(maintenanceTemplateRepository).save(any(MaintenanceTemplate.class));
    }

    @Test
    void ensureExceptionWhenModelNotFound() {
        when(modelRepository.findByModelName("A320")).thenReturn(Optional.empty());

        assertThrows(AircraftModelNotFoundException.class, () -> createMaintenanceTemplate.execute(buildRequest()));
        verify(maintenanceTemplateRepository, never()).save(any());
    }

    @Test
    void ensureExceptionWhenTemplateNameAlreadyExists() {
        when(modelRepository.findByModelName("A320")).thenReturn(Optional.of(buildModel()));
        when(maintenanceTemplateRepository.existsByName("Annual Check")).thenReturn(true);

        assertThrows(MaintenanceTemplateAlreadyExistsException.class, () -> createMaintenanceTemplate.execute(buildRequest()));
        verify(maintenanceTemplateRepository, never()).save(any());
    }
}
