package aisafe.maintenance.application;

import aisafe.aircrafts.domain.*;
import aisafe.maintenance.application.dtos.CreateMaintenanceRecordRequest;
import aisafe.maintenance.application.dtos.MaintenanceRecordResponse;
import aisafe.maintenance.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    private CreateMaintenanceRecordUseCase createMaintenanceRecord;

    private final LocalDateTime START_DATE = LocalDateTime.of(2026, 5, 23, 10, 0);

    private MaintenancePart buildPart() {
        return new MaintenancePart("P001", "Engine Filter", null, 10, 2, MaintenanceComponent.ENGINE);
    }

    private MaintenanceTemplate buildTemplate() {
        return new MaintenanceTemplate("Annual Check", MaintenanceType.INSPECTION, List.of("A320"), List.of("Check engine"), 500, 365);
    }

    private Aircraft buildAircraft() {
        AircraftModel model = new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "a320.jpg", 180);
        return new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1), model, new RegistrationNumber("CS-TPA"), 150, List.of());
    }

    private CreateMaintenanceRecordRequest buildRequest() {
        return new CreateMaintenanceRecordRequest(
                "Engine inspection", START_DATE, 4, "P001", null, "Annual Check", MaintenanceStatus.PLANNED, "CS-TPA");
    }

    @Test
    void ensureRecordIsCreatedSuccessfully() {
        MaintenancePart part = buildPart();
        MaintenanceTemplate template = buildTemplate();
        Aircraft aircraft = buildAircraft();

        when(partRepository.findByPartNumber("P001")).thenReturn(Optional.of(part));
        when(templateRepository.findByName("Annual Check")).thenReturn(Optional.of(template));
        when(aircraftRepository.findByRegistrationNumber(any(RegistrationNumber.class))).thenReturn(Optional.of(aircraft));
        when(recordRepository.existsByStartDateAndPartAndTemplate(any(), any(), any())).thenReturn(false);
        when(recordRepository.save(any(MaintenanceRecord.class))).thenAnswer(i -> i.getArguments()[0]);

        MaintenanceRecordResponse response = createMaintenanceRecord.execute(buildRequest());

        assertNotNull(response);
        assertEquals("Engine inspection", response.description());
        assertEquals("P001", response.partNumber());
        verify(recordRepository, times(1)).save(any(MaintenanceRecord.class));
    }

    @Test
    void ensureExceptionWhenPartNotFound() {
        when(partRepository.findByPartNumber("P001")).thenReturn(Optional.empty());

        assertThrows(MaintenancePartNotFoundException.class, () ->
                createMaintenanceRecord.execute(buildRequest()));
        verify(recordRepository, never()).save(any());
    }

    @Test
    void ensureExceptionWhenTemplateNotFound() {
        when(partRepository.findByPartNumber("P001")).thenReturn(Optional.of(buildPart()));
        when(templateRepository.findByName("Annual Check")).thenReturn(Optional.empty());

        assertThrows(MaintenanceTemplateNotFoundException.class, () ->
                createMaintenanceRecord.execute(buildRequest()));
        verify(recordRepository, never()).save(any());
    }

    @Test
    void ensureExceptionWhenAircraftNotFound() {
        when(partRepository.findByPartNumber("P001")).thenReturn(Optional.of(buildPart()));
        when(templateRepository.findByName("Annual Check")).thenReturn(Optional.of(buildTemplate()));
        when(aircraftRepository.findByRegistrationNumber(any(RegistrationNumber.class))).thenReturn(Optional.empty());

        assertThrows(AircraftNotFoundException.class, () ->
                createMaintenanceRecord.execute(buildRequest()));
        verify(recordRepository, never()).save(any());
    }

    @Test
    void ensureExceptionWhenDuplicateRecord() {
        when(partRepository.findByPartNumber("P001")).thenReturn(Optional.of(buildPart()));
        when(templateRepository.findByName("Annual Check")).thenReturn(Optional.of(buildTemplate()));
        when(aircraftRepository.findByRegistrationNumber(any(RegistrationNumber.class))).thenReturn(Optional.of(buildAircraft()));
        when(recordRepository.existsByStartDateAndPartAndTemplate(any(), any(), any())).thenReturn(true);

        assertThrows(MaintenanceRecordAlreadyExistsException.class, () ->
                createMaintenanceRecord.execute(buildRequest()));
        verify(recordRepository, never()).save(any());
    }
}
