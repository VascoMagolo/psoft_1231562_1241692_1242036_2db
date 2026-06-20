package aisafe.maintenance.application;

import aisafe.maintenance.application.dtos.CreateMaintenancePartRequest;
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
}
