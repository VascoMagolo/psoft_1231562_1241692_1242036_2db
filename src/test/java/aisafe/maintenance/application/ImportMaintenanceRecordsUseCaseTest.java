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
        String csvContent = "registrationNumber,template,startDate,status,components,parts,description,expectedDuration,notes,cost\n" +
                "CS-TKA,Engine Check,2023-10-01T10:00:00,PLANNED,ENGINE,\"Engine Part A,Engine Part B\",Desc 1,2,,100.00\n" +
                "CS-TKB,Landing Gear,2023-11-01T10:00:00,COMPLETED,AIRFRAME,Gear Part A,Desc 2,3,Notes,200.00";

        MockMultipartFile file = new MockMultipartFile("file", "records.csv", "text/csv", csvContent.getBytes());

        when(createMaintenanceRecordUseCase.execute(any(CreateMaintenanceRecordRequest.class))).thenReturn(null);

        BulkImportResult<MaintenanceRecordResponse> result = importUseCase.execute(file);

        assertEquals(2, result.getSuccessfulImports().size());
        assertTrue(result.isFullySuccessful());
        verify(createMaintenanceRecordUseCase, times(2)).execute(any(CreateMaintenanceRecordRequest.class));
    }

    @Test
    void ensureInvalidDateRecordsError() throws Exception {
        String csvContent = "registrationNumber,template,startDate,status,components,parts,description,expectedDuration,notes,cost\n" +
                "CS-TKA,Engine Check,invalid-date,PLANNED,ENGINE,Engine Part A,Desc,1,,10.00";

        MockMultipartFile file = new MockMultipartFile("file", "records.csv", "text/csv", csvContent.getBytes());

        BulkImportResult<MaintenanceRecordResponse> result = importUseCase.execute(file);

        assertEquals(0, result.getSuccessfulImports().size());
        assertFalse(result.isFullySuccessful());
        assertEquals(1, result.getErrors().size());
        verify(createMaintenanceRecordUseCase, never()).execute(any(CreateMaintenanceRecordRequest.class));
    }

    @Test
    void execute_EmptyFile_ReturnsError() {
        MockMultipartFile file = new MockMultipartFile("file", "records.csv", "text/csv", new byte[0]);

        BulkImportResult<MaintenanceRecordResponse> result = importUseCase.execute(file);

        assertFalse(result.isFullySuccessful());
        assertEquals(1, result.getErrors().size());
        assertEquals("CSV file is empty", result.getErrors().get(0).getErrorMessage());
    }

    @Test
    void execute_MissingHeaders_ReturnsError() {
        String csvContent = "registrationNumber,template,startDate\nCS-TKA,Engine Check,2023-10-01T10:00:00";
        MockMultipartFile file = new MockMultipartFile("file", "records.csv", "text/csv", csvContent.getBytes());

        BulkImportResult<MaintenanceRecordResponse> result = importUseCase.execute(file);

        assertFalse(result.isFullySuccessful());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("Missing required columns"));
    }

    @Test
    void execute_IoException_ReturnsError() throws Exception {
        org.springframework.web.multipart.MultipartFile file = mock(org.springframework.web.multipart.MultipartFile.class);
        when(file.getInputStream()).thenThrow(new java.io.IOException("Disk read error"));

        BulkImportResult<MaintenanceRecordResponse> result = importUseCase.execute(file);

        assertFalse(result.isFullySuccessful());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("Failed to parse CSV file"));
    }

    @Test
    void execute_MissingOtherRequiredHeaders_ReturnsError() {
        // Missing registrationNumber
        String csv1 = "template,startDate,status,components,description,expectedDuration,cost\nEngine Check,2023-10-01T10:00:00,PLANNED,ENGINE,Desc,2,100.00";
        assertFalse(importUseCase.execute(new MockMultipartFile("file", "records.csv", "text/csv", csv1.getBytes())).isFullySuccessful());

        // Missing template
        String csv2 = "registrationNumber,startDate,status,components,description,expectedDuration,cost\nCS-TKA,2023-10-01T10:00:00,PLANNED,ENGINE,Desc,2,100.00";
        assertFalse(importUseCase.execute(new MockMultipartFile("file", "records.csv", "text/csv", csv2.getBytes())).isFullySuccessful());

        // Missing startDate
        String csv3 = "registrationNumber,template,status,components,description,expectedDuration,cost\nCS-TKA,Engine Check,PLANNED,ENGINE,Desc,2,100.00";
        assertFalse(importUseCase.execute(new MockMultipartFile("file", "records.csv", "text/csv", csv3.getBytes())).isFullySuccessful());

        // Missing status
        String csv4 = "registrationNumber,template,startDate,components,description,expectedDuration,cost\nCS-TKA,Engine Check,2023-10-01T10:00:00,ENGINE,Desc,2,100.00";
        assertFalse(importUseCase.execute(new MockMultipartFile("file", "records.csv", "text/csv", csv4.getBytes())).isFullySuccessful());

        // Missing components
        String csv5 = "registrationNumber,template,startDate,status,description,expectedDuration,cost\nCS-TKA,Engine Check,2023-10-01T10:00:00,PLANNED,Desc,2,100.00";
        assertFalse(importUseCase.execute(new MockMultipartFile("file", "records.csv", "text/csv", csv5.getBytes())).isFullySuccessful());

        // Missing description
        String csv6 = "registrationNumber,template,startDate,status,components,expectedDuration,cost\nCS-TKA,Engine Check,2023-10-01T10:00:00,PLANNED,ENGINE,2,100.00";
        assertFalse(importUseCase.execute(new MockMultipartFile("file", "records.csv", "text/csv", csv6.getBytes())).isFullySuccessful());

        // Missing expectedDuration
        String csv7 = "registrationNumber,template,startDate,status,components,description,cost\nCS-TKA,Engine Check,2023-10-01T10:00:00,PLANNED,ENGINE,Desc,100.00";
        assertFalse(importUseCase.execute(new MockMultipartFile("file", "records.csv", "text/csv", csv7.getBytes())).isFullySuccessful());

        // Missing cost
        String csv8 = "registrationNumber,template,startDate,status,components,description,expectedDuration\nCS-TKA,Engine Check,2023-10-01T10:00:00,PLANNED,ENGINE,Desc,2";
        assertFalse(importUseCase.execute(new MockMultipartFile("file", "records.csv", "text/csv", csv8.getBytes())).isFullySuccessful());
    }

    @Test
    void ensureImportRowFailsWhenRequiredFieldIsEmptyOrRowTooShort() {
        // 1. Empty/missing date (throws parsing exception)
        String csv1 = "registrationNumber,template,startDate,status,components,parts,description,expectedDuration,notes,cost\n" +
                "CS-TKA,Engine Check,,PLANNED,ENGINE,Engine Part A,Desc,1,,10.00";
        var res1 = importUseCase.execute(new MockMultipartFile("file", "records.csv", "text/csv", csv1.getBytes()));
        assertEquals(1, res1.getErrors().size());

        // 2. Row has too few columns (index >= line.length)
        String csv3 = "registrationNumber,template,startDate,status,components,parts,description,expectedDuration,notes,cost\n" +
                "CS-TKA,Engine Check,2023-10-01T10:00:00";
        var res3 = importUseCase.execute(new MockMultipartFile("file", "records.csv", "text/csv", csv3.getBytes()));
        assertEquals(1, res3.getErrors().size());
    }

    @Test
    void ensureImportSucceedsWithoutOptionalHeaders() throws Exception {
        String csvContent = "registrationNumber,template,startDate,status,components,description,expectedDuration,cost,extraColumn\n" +
                "CS-TKA,Engine Check,2023-10-01T10:00:00,PLANNED,ENGINE,Desc 1,2,100.00,dummyValue";
        MockMultipartFile file = new MockMultipartFile("file", "records.csv", "text/csv", csvContent.getBytes());
        when(createMaintenanceRecordUseCase.execute(any(CreateMaintenanceRecordRequest.class))).thenReturn(null);

        BulkImportResult<MaintenanceRecordResponse> result = importUseCase.execute(file);

        assertTrue(result.isFullySuccessful());
        verify(createMaintenanceRecordUseCase).execute(any(CreateMaintenanceRecordRequest.class));
    }
}
