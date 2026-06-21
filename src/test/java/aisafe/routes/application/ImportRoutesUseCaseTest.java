package aisafe.routes.application;

import aisafe.routes.application.dtos.CreateRouteRequest;
import aisafe.shared.application.dtos.BulkImportResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportRoutesUseCaseTest {

    private CreateRouteUseCase createRouteUseCase;
    private ImportRoutesUseCase importRoutesUseCase;

    @BeforeEach
    void setUp() {
        createRouteUseCase = mock(CreateRouteUseCase.class);
        importRoutesUseCase = new ImportRoutesUseCase(createRouteUseCase);
    }

    @Test
    void ensureImportValidRoutes() throws Exception {
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
    void ensureHandleMissingColumns() throws Exception {
        String csvContent = "originIataCode,estimatedFlightTime,minimumRange,minimumCapacity\n" +
                            "LIS,60,300,100";
        MockMultipartFile file = new MockMultipartFile("file", "routes.csv", "text/csv", csvContent.getBytes());

        BulkImportResult<String> result = importRoutesUseCase.execute(file, "Admin");

        assertFalse(result.getErrors().isEmpty());
        assertEquals("Missing required columns (originIataCode, destinationIataCode, estimatedFlightTime, minimumRange, minimumCapacity)", result.getErrors().get(0).getErrorMessage());
    }

    @Test
    void ensureImportEmptyCsvFileReturnsError() {
        MockMultipartFile file = new MockMultipartFile("file", "routes.csv", "text/csv", new byte[0]);

        BulkImportResult<String> result = importRoutesUseCase.execute(file, "Admin");

        assertFalse(result.isFullySuccessful());
        assertEquals("CSV file is empty", result.getErrors().get(0).getErrorMessage());
    }

    @Test
    void ensureImportWithNullCreatedByDefaultsToBulkImport() {
        String csvContent = "originIataCode,destinationIataCode,estimatedFlightTime,minimumRange,minimumCapacity\n" +
                            "LIS,OPO,60,300,100";
        MockMultipartFile file = new MockMultipartFile("file", "routes.csv", "text/csv", csvContent.getBytes());

        ArgumentCaptor<CreateRouteRequest> captor = ArgumentCaptor.forClass(CreateRouteRequest.class);
        when(createRouteUseCase.execute(captor.capture())).thenReturn(null);

        BulkImportResult<String> result = importRoutesUseCase.execute(file, null);

        assertTrue(result.isFullySuccessful());
        assertEquals("BulkImport", captor.getValue().createdBy());
    }

    @Test
    void ensureImportHandlesRowExceptionsAndNumberParsingErrors() {
        // Row 2: invalid flight time (abc)
        // Row 3: invalid minimum range (xyz)
        // Row 4: invalid minimum capacity (invalid)
        // Row 5: missing minimum capacity column (out of bounds)
        // Row 6: empty minimum capacity value (empty)
        // Row 7: empty estimatedFlightTime (null)
        // Row 8: empty minimumRange (null)
        String csvContent = "originIataCode,destinationIataCode,estimatedFlightTime,minimumRange,minimumCapacity\n" +
                            "LIS,OPO,abc,300,100\n" +
                            "LIS,MAD,90,xyz,150\n" +
                            "LIS,BCN,90,300,invalid\n" +
                            "LIS,CDG,60,300\n" +
                            "LIS,LHR,60,300, \n" +
                            "LIS,OPO,,300,100\n" +
                            "LIS,OPO,60,,100";
        MockMultipartFile file = new MockMultipartFile("file", "routes.csv", "text/csv", csvContent.getBytes());

        when(createRouteUseCase.execute(any(CreateRouteRequest.class))).thenAnswer(invocation -> {
            CreateRouteRequest req = invocation.getArgument(0);
            if (req.estimatedFlightTime() == null || req.minimumRange() == null || req.minimumCapacity() == null) {
                throw new IllegalArgumentException("Invalid route data");
            }
            return null;
        });

        BulkImportResult<String> result = importRoutesUseCase.execute(file, "Admin");

        assertFalse(result.isFullySuccessful());
        assertEquals(7, result.getTotalRowsProcessed());
        assertEquals(7, result.getErrors().size());
        assertEquals("Invalid route data", result.getErrors().get(0).getErrorMessage());
    }

    @Test
    void ensureImportIoExceptionReturnsError() throws Exception {
        org.springframework.web.multipart.MultipartFile file = mock(org.springframework.web.multipart.MultipartFile.class);
        when(file.getInputStream()).thenThrow(new java.io.IOException("Disk read error"));

        BulkImportResult<String> result = importRoutesUseCase.execute(file, "Admin");

        assertFalse(result.isFullySuccessful());
        assertEquals("Failed to parse CSV file: Disk read error", result.getErrors().get(0).getErrorMessage());
    }

    @Test
    void ensureHandleMissingOtherColumns() {
        // Missing originIataCode
        String csv1 = "destinationIataCode,estimatedFlightTime,minimumRange,minimumCapacity\nOPO,60,300,100";
        assertFalse(importRoutesUseCase.execute(new MockMultipartFile("file", "routes.csv", "text/csv", csv1.getBytes()), "Admin").isFullySuccessful());

        // Missing estimatedFlightTime
        String csv2 = "originIataCode,destinationIataCode,minimumRange,minimumCapacity\nLIS,OPO,300,100";
        assertFalse(importRoutesUseCase.execute(new MockMultipartFile("file", "routes.csv", "text/csv", csv2.getBytes()), "Admin").isFullySuccessful());

        // Missing minimumRange
        String csv3 = "originIataCode,destinationIataCode,estimatedFlightTime,minimumCapacity\nLIS,OPO,60,100";
        assertFalse(importRoutesUseCase.execute(new MockMultipartFile("file", "routes.csv", "text/csv", csv3.getBytes()), "Admin").isFullySuccessful());

        // Missing minimumCapacity
        String csv4 = "originIataCode,destinationIataCode,estimatedFlightTime,minimumRange\nLIS,OPO,60,300";
        assertFalse(importRoutesUseCase.execute(new MockMultipartFile("file", "routes.csv", "text/csv", csv4.getBytes()), "Admin").isFullySuccessful());
    }
}
