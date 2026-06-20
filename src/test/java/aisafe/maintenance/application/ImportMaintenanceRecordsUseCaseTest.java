package aisafe.maintenance.application;

import aisafe.maintenance.application.dtos.CreateMaintenanceRecordRequest;
import aisafe.maintenance.application.dtos.MaintenanceRecordResponse;
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
class ImportMaintenanceRecordsUseCaseTest {

    @Mock
    private CreateMaintenanceRecordUseCase createMaintenanceRecordUseCase;

    private ImportMaintenanceRecordsUseCase importUseCase;

    @BeforeEach
    void setUp() {
        importUseCase = new ImportMaintenanceRecordsUseCase(createMaintenanceRecordUseCase);
    }

    @Test
    void ensureSuccessfulImport() throws Exception {
        String csvContent = "aircraftRegistration,templateName,date,status,components,parts\n" +
                "CS-TKA,Engine Check,2023-10-01T10:00:00,PLANNED,ENGINE,\"Engine Part A,Engine Part B\"\n" +
                "CS-TKB,Landing Gear,2023-11-01T10:00:00,COMPLETED,AIRFRAME,Gear Part A";

        MockMultipartFile file = new MockMultipartFile("file", "records.csv", "text/csv", csvContent.getBytes());

        when(createMaintenanceRecordUseCase.execute(any(CreateMaintenanceRecordRequest.class))).thenReturn(null);

        BulkImportResult<MaintenanceRecordResponse> result = importUseCase.execute(file);

        assertEquals(2, result.getSuccessfulImports().size());
        assertTrue(result.isFullySuccessful());
        verify(createMaintenanceRecordUseCase, times(2)).execute(any(CreateMaintenanceRecordRequest.class));
    }

    @Test
    void ensureInvalidDateRecordsError() throws Exception {
        String csvContent = "aircraftRegistration,templateName,date,status,components,parts\n" +
                "CS-TKA,Engine Check,invalid-date,PLANNED,ENGINE,Engine Part A";

        MockMultipartFile file = new MockMultipartFile("file", "records.csv", "text/csv", csvContent.getBytes());

        BulkImportResult<MaintenanceRecordResponse> result = importUseCase.execute(file);

        assertEquals(0, result.getSuccessfulImports().size());
        assertFalse(result.isFullySuccessful());
        assertEquals(1, result.getErrors().size());
        verify(createMaintenanceRecordUseCase, never()).execute(any(CreateMaintenanceRecordRequest.class));
    }
}
