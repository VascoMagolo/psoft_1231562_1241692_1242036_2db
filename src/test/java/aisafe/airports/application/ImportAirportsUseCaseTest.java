package aisafe.airports.application;

import aisafe.airports.application.dtos.RegisterAirportRequest;
import aisafe.shared.application.dtos.BulkImportResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportAirportsUseCaseTest {

    @Mock
    private RegisterAirportUseCase registerAirportUseCase;

    @InjectMocks
    private ImportAirportsUseCase importAirportsUseCase;

    @Test
    void ensureCsvImportParsesSuccessfully() {
        String csvData = "iataCode,name,city,country,region,timezone,latitude,longitude\n" +
                "OPO,Francisco Sa Carneiro,Porto,Portugal,Europe,Europe/Lisbon,41.2481,-8.6814\n" +
                "LIS,Humberto Delgado,Lisbon,Portugal,Europe,Europe/Lisbon,38.7742,-9.1342";
        
        MockMultipartFile file = new MockMultipartFile("file", "airports.csv", "text/csv", csvData.getBytes());

        when(registerAirportUseCase.execute(any(RegisterAirportRequest.class))).thenReturn(null);

        BulkImportResult<String> result = importAirportsUseCase.execute(file);

        assertTrue(result.isFullySuccessful());
        assertEquals(2, result.getTotalRowsProcessed());
        assertEquals(2, result.getSuccessfulImports().size());
        assertTrue(result.getErrors().isEmpty());

        verify(registerAirportUseCase, times(2)).execute(any(RegisterAirportRequest.class));
    }

    @Test
    void ensureCsvImportHandlesErrorsInRows() {
        String csvData = "iataCode,name,city,country,region,timezone,latitude,longitude\n" +
                "OPO,Francisco Sa Carneiro,Porto,Portugal,Europe,Europe/Lisbon,41.2481,-8.6814\n" +
                "LIS,Humberto Delgado,Lisbon,Portugal,Europe,Europe/Lisbon,INVALID_LAT,-9.1342";
        
        MockMultipartFile file = new MockMultipartFile("file", "airports.csv", "text/csv", csvData.getBytes());

        when(registerAirportUseCase.execute(any(RegisterAirportRequest.class))).thenReturn(null);

        // For LIS, mock an exception
        when(registerAirportUseCase.execute(argThat(req -> "LIS".equals(req.iataCode()))))
                .thenThrow(new RuntimeException("Invalid latitude"));

        BulkImportResult<String> result = importAirportsUseCase.execute(file);

        assertFalse(result.isFullySuccessful());
        assertEquals(2, result.getTotalRowsProcessed());
        assertEquals(1, result.getSuccessfulImports().size());
        assertEquals(1, result.getErrors().size());
        assertEquals("OPO", result.getSuccessfulImports().get(0));
        assertEquals("Invalid latitude", result.getErrors().get(0).getErrorMessage());
    }
}
