package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.RegisterAircraftRequest;
import aisafe.aircrafts.application.dtos.AircraftResponse;
import aisafe.shared.application.dtos.BulkImportResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportAircraftsUseCaseTest {

    @Mock
    private RegisterAircraftUseCase registerUseCase;

    @InjectMocks
    private ImportAircraftsUseCase useCase;

    @Test
    void ensureImportValidAircraftsSuccessfully() {
        String csvData = "registrationNumber,modelName,status,manufacturingDate,range,seatCapacity,features\n" +
                "PT-ABC,Boeing 737,AVAILABLE,2020-01-01,3000.0,150,WiFi;Screens";
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csvData.getBytes(StandardCharsets.UTF_8));
        
        when(registerUseCase.execute(any(RegisterAircraftRequest.class))).thenReturn(mock(AircraftResponse.class));
        
        BulkImportResult<AircraftResponse> result = useCase.execute(file);
        
        assertTrue(result.isFullySuccessful());
        assertEquals(1, result.getSuccessfulImports().size());
        assertEquals(0, result.getErrors().size());
        verify(registerUseCase, times(1)).execute(any(RegisterAircraftRequest.class));
    }

    @Test
    void ensureRecordErrorWhenRowIsInvalid() {
        String csvData = "registrationNumber,modelName,status,manufacturingDate,range,seatCapacity,features\n" +
                "PT-ABC,Boeing 737,AVAILABLE,INVALID_DATE,3000.0,150,WiFi;Screens";
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csvData.getBytes(StandardCharsets.UTF_8));
        
        BulkImportResult<AircraftResponse> result = useCase.execute(file);
        
        assertFalse(result.isFullySuccessful());
        assertEquals(0, result.getSuccessfulImports().size());
        assertEquals(1, result.getErrors().size());
        verify(registerUseCase, never()).execute(any());
    }

    @Test
    void ensureEmptyCsvReturnsEmptyResult() {
        MultipartFile file = new MockMultipartFile("file", "empty.csv", "text/csv", new byte[0]);

        BulkImportResult<AircraftResponse> result = useCase.execute(file);

        assertEquals(0, result.getSuccessfulImports().size());
        assertEquals(0, result.getErrors().size());
    }

    @Test
    void ensureRecordErrorWhenRowHasTooFewColumns() {
        String csvData = "registrationNumber,modelName,status,manufacturingDate,range,seatCapacity,features\n" +
                "PT-ABC,Boeing 737,AVAILABLE";
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csvData.getBytes(StandardCharsets.UTF_8));

        BulkImportResult<AircraftResponse> result = useCase.execute(file);

        assertEquals(0, result.getSuccessfulImports().size());
        assertEquals(1, result.getErrors().size());
        verify(registerUseCase, never()).execute(any());
    }

    @Test
    void ensureImportSucceedsWithoutFeaturesColumn() {
        String csvData = "registrationNumber,modelName,status,manufacturingDate,range,seatCapacity\n" +
                "PT-ABC,Boeing 737,AVAILABLE,2020-01-01,3000.0,150";
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csvData.getBytes(StandardCharsets.UTF_8));

        when(registerUseCase.execute(any(RegisterAircraftRequest.class))).thenReturn(mock(AircraftResponse.class));

        BulkImportResult<AircraftResponse> result = useCase.execute(file);

        assertEquals(1, result.getSuccessfulImports().size());
        assertEquals(0, result.getErrors().size());
    }

    @Test
    void ensureThrowsExceptionWhenFileReadingFails() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenThrow(new java.io.IOException("Disk read error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> useCase.execute(file));
        assertEquals("Failed to process CSV file", exception.getMessage());
        assertTrue(exception.getCause() instanceof java.io.IOException);
    }

    @Test
    void ensureRecordErrorWhenExceptionHasNullMessage() {
        String csvData = "registrationNumber,modelName,status,manufacturingDate,range,seatCapacity,features\n" +
                "PT-ABC,Boeing 737,AVAILABLE,2020-01-01,3000.0,150,WiFi;Screens";
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csvData.getBytes(StandardCharsets.UTF_8));
        
        when(registerUseCase.execute(any(RegisterAircraftRequest.class))).thenThrow(new NullPointerException());
        
        BulkImportResult<AircraftResponse> result = useCase.execute(file);
        
        assertFalse(result.isFullySuccessful());
        assertEquals(1, result.getErrors().size());
        assertEquals("Unknown error", result.getErrors().get(0).getErrorMessage());
    }
}
