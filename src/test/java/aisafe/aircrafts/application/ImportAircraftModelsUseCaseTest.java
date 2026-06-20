package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.AircraftModelResponse;
import aisafe.aircrafts.application.dtos.RegisterAircraftModelRequest;
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
class ImportAircraftModelsUseCaseTest {

    @Mock
    private RegisterAircraftModelUseCase registerUseCase;

    @InjectMocks
    private ImportAircraftModelsUseCase useCase;

    @Test
    void ensureImportValidAircraftModelsSuccessfully() {
        String csvData = "modelName,manufacturer,maximumTakeoffWeight,maximumPayloadCapacity,cruiseSpeed,maximumSeatCapacity\n" +
                "Boeing 737,BOEING,1000.0,2000.0,500.0,150";
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csvData.getBytes(StandardCharsets.UTF_8));
        
        when(registerUseCase.execute(any(RegisterAircraftModelRequest.class))).thenReturn(mock(AircraftModelResponse.class));
        
        BulkImportResult<AircraftModelResponse> result = useCase.execute(file);
        
        assertTrue(result.isFullySuccessful());
        assertEquals(1, result.getSuccessfulImports().size());
        assertEquals(0, result.getErrors().size());
        verify(registerUseCase, times(1)).execute(any(RegisterAircraftModelRequest.class));
    }

    @Test
    void ensureRecordErrorWhenRowIsInvalid() {
        String csvData = "modelName,manufacturer,maximumTakeoffWeight,maximumPayloadCapacity,cruiseSpeed,maximumSeatCapacity\n" +
                "Boeing 737,INVALID_MANUF,1000.0,2000.0,500.0,150";
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csvData.getBytes(StandardCharsets.UTF_8));
        
        BulkImportResult<AircraftModelResponse> result = useCase.execute(file);
        
        assertFalse(result.isFullySuccessful());
        assertEquals(0, result.getSuccessfulImports().size());
        assertEquals(1, result.getErrors().size());
        verify(registerUseCase, never()).execute(any());
    }
}
