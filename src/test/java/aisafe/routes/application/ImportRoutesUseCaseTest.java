package aisafe.routes.application;

import aisafe.routes.application.dtos.CreateRouteRequest;
import aisafe.shared.application.dtos.BulkImportResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ImportRoutesUseCaseTest {

    private CreateRouteUseCase createRouteUseCase;
    private ImportRoutesUseCase importRoutesUseCase;

    @BeforeEach
    void setUp() {
        createRouteUseCase = mock(CreateRouteUseCase.class);
        importRoutesUseCase = new ImportRoutesUseCase(createRouteUseCase);
    }

    @Test
    void shouldImportValidRoutes() throws Exception {
        String csvContent = "originIataCode,destinationIataCode,estimatedFlightTime,minimumRange,minimumCapacity\n" +
                            "LIS,OPO,60,300,100\n" +
                            "LIS,MAD,90,500,150";
        MockMultipartFile file = new MockMultipartFile("file", "routes.csv", "text/csv", csvContent.getBytes());

        BulkImportResult<String> result = importRoutesUseCase.execute(file, "Admin");

        assertEquals(2, result.getSuccessfulImports().size());
        assertTrue(result.getErrors().isEmpty());
        verify(createRouteUseCase, times(2)).execute(any());
    }

    @Test
    void shouldHandleMissingColumns() throws Exception {
        String csvContent = "originIataCode,estimatedFlightTime,minimumRange,minimumCapacity\n" +
                            "LIS,60,300,100";
        MockMultipartFile file = new MockMultipartFile("file", "routes.csv", "text/csv", csvContent.getBytes());

        BulkImportResult<String> result = importRoutesUseCase.execute(file, "Admin");

        assertFalse(result.getErrors().isEmpty());
        assertEquals("Missing required columns (originIataCode, destinationIataCode, estimatedFlightTime, minimumRange, minimumCapacity)", result.getErrors().get(0).getErrorMessage());
    }
}
