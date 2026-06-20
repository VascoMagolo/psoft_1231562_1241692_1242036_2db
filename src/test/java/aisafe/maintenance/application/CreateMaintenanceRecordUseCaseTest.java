package aisafe.maintenance.application;

import aisafe.aircrafts.domain.*;
import aisafe.maintenance.application.dtos.CreateMaintenanceRecordRequest;
import aisafe.maintenance.application.dtos.MaintenanceRecordResponse;
import aisafe.maintenance.domain.*;
import aisafe.shared.domain.DuplicateResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateMaintenanceRecordUseCaseTest {

    @Mock
    private MaintenanceRecordRepository recordRepository;
    @Mock
    private MaintenancePartRepository partRepository;
    @Mock
    private MaintenanceTemplateRepository templateRepository;
    @Mock
    private AircraftRepository aircraftRepository;

    @InjectMocks
    private CreateMaintenanceRecordUseCase useCase;

    private CreateMaintenanceRecordRequest request;
    private MaintenancePart part;
    private MaintenanceTemplate template;
    private Aircraft aircraft;

    @BeforeEach
    void setUp() {
        request = new CreateMaintenanceRecordRequest(
                "Test maintenance", LocalDateTime.of(2023, 1, 1, 10, 0), 8, List.of("P001"), "Notes", "Template1", MaintenanceStatus.PLANNED, "CS-TPA", Set.of(MaintenanceComponent.ENGINE), BigDecimal.valueOf(500)
        );
        part = new MaintenancePart("P001", "Part1", "Desc1", 10, 2, MaintenanceComponent.ENGINE);
        template = new MaintenanceTemplate("Template1", MaintenanceType.INSPECTION, List.of(new ModelName("Model1")), List.of("Check1"), 100, 30);
        AircraftModel model = new AircraftModel("Model1", Manufacturer.AIRBUS, 1000.0, 5000.0, 800.0, null, 180);
        aircraft = new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2023, 1, 1), model, new RegistrationNumber("CS-TPA"), 150, 4000.0, List.of());
    }

    @Test
    void ensureRecordIsCreatedSuccessfully() {
        when(partRepository.findByPartNumber("P001")).thenReturn(Optional.of(part));
        when(templateRepository.findByName("Template1")).thenReturn(Optional.of(template));
        when(aircraftRepository.findByRegistrationNumber(any())).thenReturn(Optional.of(aircraft));
        when(recordRepository.existsByStartDateAndTemplate(any(), any())).thenReturn(false);

        MaintenanceRecordResponse response = useCase.execute(request);

        assertNotNull(response);
        assertEquals("CS-TPA", response.aircraftRegistration());
        assertEquals(Set.of("ENGINE"), response.components());
        verify(recordRepository, times(1)).save(any(MaintenanceRecord.class));
    }

    @Test
    void ensureExceptionWhenRecordAlreadyExists() {
        when(partRepository.findByPartNumber("P001")).thenReturn(Optional.of(part));
        when(templateRepository.findByName("Template1")).thenReturn(Optional.of(template));
        when(aircraftRepository.findByRegistrationNumber(any())).thenReturn(Optional.of(aircraft));
        when(recordRepository.existsByStartDateAndTemplate(any(), any())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> useCase.execute(request));
        verify(recordRepository, never()).save(any());
    }
}
