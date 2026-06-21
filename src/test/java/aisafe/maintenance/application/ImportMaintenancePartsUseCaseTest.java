package aisafe.maintenance.application;

import aisafe.maintenance.application.dtos.CreateMaintenancePartRequest;
import java.io.IOException;
import aisafe.maintenance.domain.MaintenanceComponent;
import aisafe.shared.application.dtos.BulkImportResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportMaintenancePartsUseCaseTest {

    @Mock
    private CreateMaintenancePartUseCase createMaintenancePartUseCase;

    private ImportMaintenancePartsUseCase importUseCase;

    @BeforeEach
    void setUp() {
        importUseCase = new ImportMaintenancePartsUseCase(createMaintenancePartUseCase);
    }

    @Test
    void execute_SuccessfulImport() throws Exception {
        String csvContent = "partNumber,name,description,stockQuantity,minimumThreshold,component\n" +
                "JP-1001,Japanese Starter Motor,High efficiency motor,50,5,ENGINE\n" +
                "JP-1002,Landing Gear Tire,Durable tire,100,10,AIRFRAME";

        MockMultipartFile file = new MockMultipartFile("file", "parts.csv", "text/csv", csvContent.getBytes());

        when(createMaintenancePartUseCase.execute(any(CreateMaintenancePartRequest.class))).thenReturn(null);

        BulkImportResult<String> result = importUseCase.execute(file);

        assertEquals(2, result.getSuccessfulImports().size());
        assertTrue(result.isFullySuccessful());
        verify(createMaintenancePartUseCase, times(2)).execute(any(CreateMaintenancePartRequest.class));
    }

    @Test
    void execute_InvalidData_RecordsError() throws Exception {
        String csvContent = "partNumber,name,description,stockQuantity,minimumThreshold,component\n" +
                "JP-1001,Japanese Starter Motor,High efficiency motor,invalid_stock,5,ENGINE\n" +
                "JP-1002,Landing Gear Tire,Durable tire,100,invalid_threshold,AIRFRAME";

        MockMultipartFile file = new MockMultipartFile("file", "parts.csv", "text/csv", csvContent.getBytes());

        BulkImportResult<String> result = importUseCase.execute(file);

        assertEquals(0, result.getSuccessfulImports().size());
        assertFalse(result.isFullySuccessful());
        assertEquals(2, result.getErrors().size());
        verify(createMaintenancePartUseCase, never()).execute(any(CreateMaintenancePartRequest.class));
    }

    @Test
    void execute_EmptyFile_ReturnsError() {
        MockMultipartFile file = new MockMultipartFile("file", "parts.csv", "text/csv", new byte[0]);

        BulkImportResult<String> result = importUseCase.execute(file);

        assertFalse(result.isFullySuccessful());
        assertEquals(1, result.getErrors().size());
        assertEquals("CSV file is empty", result.getErrors().get(0).getErrorMessage());
    }

    @Test
    void execute_MissingHeaders_ReturnsError() {
        String csvContent = "partNumber,name,description,stockQuantity,minimumThreshold\nJP-1001,Japanese Starter Motor,Motor,50,5";
        MockMultipartFile file = new MockMultipartFile("file", "parts.csv", "text/csv", csvContent.getBytes());

        BulkImportResult<String> result = importUseCase.execute(file);

        assertFalse(result.isFullySuccessful());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("Missing required columns"));
    }

    @Test
    void execute_IoException_ReturnsError() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenThrow(new IOException("Disk read error"));

        BulkImportResult<String> result = importUseCase.execute(file);

        assertFalse(result.isFullySuccessful());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("Failed to parse CSV file"));
    }

    @Test
    void ensureImportSucceedsWithNullOptionalFields() throws Exception {
        String csvContent = "partNumber,name,description,stockQuantity,minimumThreshold,component\n" +
                "JP-1003,Starter,,50,5,ENGINE";

        MockMultipartFile file = new MockMultipartFile("file", "parts.csv", "text/csv", csvContent.getBytes());
        when(createMaintenancePartUseCase.execute(any(CreateMaintenancePartRequest.class))).thenReturn(null);

        BulkImportResult<String> result = importUseCase.execute(file);

        assertEquals(1, result.getSuccessfulImports().size());
        assertTrue(result.isFullySuccessful());
        verify(createMaintenancePartUseCase).execute(new CreateMaintenancePartRequest("JP-1003", "Starter", null, 50, 5, MaintenanceComponent.ENGINE));
    }

    @Test
    void ensureImportRowFailsWhenRequiredFieldIsEmpty() throws Exception {
        String csvContent = "partNumber,name,description,stockQuantity,minimumThreshold,component\n" +
                "JP-1004,,Desc,50,5,ENGINE";

        MockMultipartFile file = new MockMultipartFile("file", "parts.csv", "text/csv", csvContent.getBytes());
        when(createMaintenancePartUseCase.execute(any())).thenThrow(new IllegalArgumentException("Name cannot be empty"));

        BulkImportResult<String> result = importUseCase.execute(file);

        assertEquals(0, result.getSuccessfulImports().size());
        assertFalse(result.isFullySuccessful());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("Name cannot be empty"));
    }

    @Test
    void ensureImportRowFailsWhenRowHasTooFewColumns() throws Exception {
        String csvContent = "partNumber,name,description,stockQuantity,minimumThreshold,component\n" +
                "JP-1005,Starter,Desc"; // missing columns

        MockMultipartFile file = new MockMultipartFile("file", "parts.csv", "text/csv", csvContent.getBytes());
        when(createMaintenancePartUseCase.execute(any())).thenThrow(new IllegalArgumentException("Required fields missing"));

        BulkImportResult<String> result = importUseCase.execute(file);

        assertEquals(0, result.getSuccessfulImports().size());
        assertFalse(result.isFullySuccessful());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("Required fields missing"));
    }

    @Test
    void ensureImportRowFailsWhenComponentIsInvalid() throws Exception {
        String csvContent = "partNumber,name,description,stockQuantity,minimumThreshold,component\n" +
                "JP-1006,Starter,Desc,50,5,INVALID_COMP";

        MockMultipartFile file = new MockMultipartFile("file", "parts.csv", "text/csv", csvContent.getBytes());

        BulkImportResult<String> result = importUseCase.execute(file);

        assertEquals(0, result.getSuccessfulImports().size());
        assertFalse(result.isFullySuccessful());
        assertEquals(1, result.getErrors().size());
    }

    @Test
    void execute_MissingOtherHeaders_ReturnsError() {
        // Missing partNumber
        String csv1 = "name,description,stockQuantity,minimumThreshold,component\nStarter,Motor,50,5,ENGINE";
        assertFalse(importUseCase.execute(new MockMultipartFile("file", "parts.csv", "text/csv", csv1.getBytes())).isFullySuccessful());

        // Missing name
        String csv2 = "partNumber,description,stockQuantity,minimumThreshold,component\nJP-1001,Motor,50,5,ENGINE";
        assertFalse(importUseCase.execute(new MockMultipartFile("file", "parts.csv", "text/csv", csv2.getBytes())).isFullySuccessful());

        // Missing stockQuantity
        String csv3 = "partNumber,name,description,minimumThreshold,component\nJP-1001,Starter,Motor,5,ENGINE";
        assertFalse(importUseCase.execute(new MockMultipartFile("file", "parts.csv", "text/csv", csv3.getBytes())).isFullySuccessful());

        // Missing minimumThreshold
        String csv4 = "partNumber,name,description,stockQuantity,component\nJP-1001,Starter,Motor,50,ENGINE";
        assertFalse(importUseCase.execute(new MockMultipartFile("file", "parts.csv", "text/csv", csv4.getBytes())).isFullySuccessful());

        // Missing component
        String csv5 = "partNumber,name,description,stockQuantity,minimumThreshold\nJP-1001,Starter,Motor,50,5";
        assertFalse(importUseCase.execute(new MockMultipartFile("file", "parts.csv", "text/csv", csv5.getBytes())).isFullySuccessful());
    }

    @Test
    void ensureImportSucceedsWithoutDescriptionHeader() throws Exception {
        String csvContent = "partNumber,name,stockQuantity,minimumThreshold,component\n" +
                "JP-1007,Starter,50,5,ENGINE";
        MockMultipartFile file = new MockMultipartFile("file", "parts.csv", "text/csv", csvContent.getBytes());
        when(createMaintenancePartUseCase.execute(any(CreateMaintenancePartRequest.class))).thenReturn(null);

        BulkImportResult<String> result = importUseCase.execute(file);

        assertTrue(result.isFullySuccessful());
        assertEquals(1, result.getSuccessfulImports().size());
        verify(createMaintenancePartUseCase).execute(new CreateMaintenancePartRequest("JP-1007", "Starter", null, 50, 5, MaintenanceComponent.ENGINE));
    }

    @Test
    void ensureImportSucceedsWithNullComponent() throws Exception {
        String csvContent = "partNumber,name,description,stockQuantity,minimumThreshold,component,extraColumn\n" +
                "JP-1008,Starter,Desc,50,5,,dummyValue";
        MockMultipartFile file = new MockMultipartFile("file", "parts.csv", "text/csv", csvContent.getBytes());
        when(createMaintenancePartUseCase.execute(any(CreateMaintenancePartRequest.class))).thenReturn(null);

        BulkImportResult<String> result = importUseCase.execute(file);

        assertTrue(result.isFullySuccessful());
        verify(createMaintenancePartUseCase).execute(new CreateMaintenancePartRequest("JP-1008", "Starter", "Desc", 50, 5, null));
    }
}
