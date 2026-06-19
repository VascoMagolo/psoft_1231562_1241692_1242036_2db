package aisafe.maintenance.application;

import aisafe.maintenance.application.dtos.CreateMaintenanceTemplateRequest;
import aisafe.maintenance.application.dtos.MaintenanceTemplateResponse;
import aisafe.shared.application.dtos.BulkImportResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportMaintenanceTemplatesUseCaseTest {

    @Mock
    private CreateMaintenanceTemplateUseCase createMaintenanceTemplateUseCase;

    private ImportMaintenanceTemplatesUseCase importUseCase;

    @BeforeEach
    void setUp() {
        importUseCase = new ImportMaintenanceTemplatesUseCase(createMaintenanceTemplateUseCase);
    }

    @Test
    void execute_SuccessfulImport() throws Exception {
        String csvContent = "templateName,description,flightHoursThreshold,monthsThreshold,flightCyclesThreshold,models,checklist\n" +
                "Engine Check,Routine engine check,1000,12,500,A320;B737,Check oil,Inspect blades\n" +
                "Landing Gear Inspection,Inspect landing gear,2000,24,1000,A320,Check tire pressure,Check brakes";

        MockMultipartFile file = new MockMultipartFile("file", "templates.csv", "text/csv", csvContent.getBytes());

        when(createMaintenanceTemplateUseCase.execute(any(CreateMaintenanceTemplateRequest.class))).thenReturn(null);

        BulkImportResult<MaintenanceTemplateResponse> result = importUseCase.execute(file);

        assertEquals(2, result.getSuccessfulImports().size());
        assertTrue(result.isFullySuccessful());
        verify(createMaintenanceTemplateUseCase, times(2)).execute(any(CreateMaintenanceTemplateRequest.class));
    }

    @Test
    void execute_InvalidData_RecordsError() throws Exception {
        String csvContent = "templateName,description,flightHoursThreshold,monthsThreshold,flightCyclesThreshold,models,checklist\n" +
                "Engine Check,invalid_hours,invalid_months,500,A320;B737,Check oil";

        MockMultipartFile file = new MockMultipartFile("file", "templates.csv", "text/csv", csvContent.getBytes());

        BulkImportResult<MaintenanceTemplateResponse> result = importUseCase.execute(file);

        assertEquals(0, result.getSuccessfulImports().size());
        assertFalse(result.isFullySuccessful());
        assertEquals(1, result.getErrors().size());
        verify(createMaintenanceTemplateUseCase, never()).execute(any(CreateMaintenanceTemplateRequest.class));
    }
}
