package aisafe.flights.application;

import aisafe.flights.application.dtos.ScheduleFlightRequest;
import aisafe.shared.application.dtos.BulkImportResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ImportFlightsUseCaseTest {

    private ScheduleFlightUseCase scheduleFlightUseCase;
    private ImportFlightsUseCase importFlightsUseCase;

    @BeforeEach
    void setUp() {
        scheduleFlightUseCase = mock(ScheduleFlightUseCase.class);
        importFlightsUseCase = new ImportFlightsUseCase(scheduleFlightUseCase);
    }

    @Test
    void ensureImportValidFlights() throws Exception {
        String csvContent = "flightNumber,routeOrigin,routeDestination,aircraftRegistration,departureDate,arrivalDate,status\n" +
                            "TP100,LIS,OPO,CS-TVA,2026-06-19T10:00:00Z,2026-06-19T11:00:00Z,SCHEDULED";
        MockMultipartFile file = new MockMultipartFile("file", "flights.csv", "text/csv", csvContent.getBytes());

        BulkImportResult<String> result = importFlightsUseCase.execute(file);

        assertEquals(1, result.getSuccessfulImports().size());
        assertTrue(result.getErrors().isEmpty());
        verify(scheduleFlightUseCase, times(1)).execute(any());
    }

    @Test
    void ensureImportEmptyCsvReturnsError() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.csv", "text/csv", new byte[0]);

        BulkImportResult<String> result = importFlightsUseCase.execute(file);

        assertEquals(0, result.getSuccessfulImports().size());
        assertEquals(1, result.getErrors().size());
        assertEquals("CSV file is empty", result.getErrors().get(0).getErrorMessage());
    }

    @Test
    void ensureImportMissingHeadersReturnsError() {
        String csvContent = "flightNumber,routeOrigin,routeDestination\nTP100,LIS,OPO";
        MockMultipartFile file = new MockMultipartFile("file", "missing_headers.csv", "text/csv", csvContent.getBytes());

        BulkImportResult<String> result = importFlightsUseCase.execute(file);

        assertEquals(0, result.getSuccessfulImports().size());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("Missing required columns"));
    }

    @Test
    void ensureImportInvalidDateReturnsError() {
        String csvContent = "flightNumber,routeOrigin,routeDestination,aircraftRegistration,departureDate,arrivalDate,status\n" +
                            "TP100,LIS,OPO,CS-TVA,invalid-date,2026-06-19T11:00:00Z,SCHEDULED";
        MockMultipartFile file = new MockMultipartFile("file", "invalid_date.csv", "text/csv", csvContent.getBytes());

        BulkImportResult<String> result = importFlightsUseCase.execute(file);

        assertEquals(0, result.getSuccessfulImports().size());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("Invalid date format"));
    }

    @Test
    void ensureImportSuccessWithoutFlightNumberReturnsDefaultIdentifier() {
        String csvContent = "flightNumber,routeOrigin,routeDestination,aircraftRegistration,departureDate,arrivalDate,status\n" +
                            ",LIS,OPO,CS-TVA,2026-06-19T10:00:00Z,2026-06-19T11:00:00Z,SCHEDULED";
        MockMultipartFile file = new MockMultipartFile("file", "no_flight_num.csv", "text/csv", csvContent.getBytes());

        BulkImportResult<String> result = importFlightsUseCase.execute(file);

        assertEquals(1, result.getSuccessfulImports().size());
        assertEquals("CS-TVA-LIS-OPO", result.getSuccessfulImports().get(0));
    }

    @Test
    void ensureImportExceptionsRecordedAsErrors() {
        String csvContent = "flightNumber,routeOrigin,routeDestination,aircraftRegistration,departureDate,arrivalDate,status\n" +
                            "TP100,LIS,OPO,CS-TVA,2026-06-19T10:00:00Z,2026-06-19T11:00:00Z,SCHEDULED";
        MockMultipartFile file = new MockMultipartFile("file", "error.csv", "text/csv", csvContent.getBytes());

        doThrow(new RuntimeException("Schedule Error")).when(scheduleFlightUseCase).execute(any());

        BulkImportResult<String> result = importFlightsUseCase.execute(file);

        assertEquals(0, result.getSuccessfulImports().size());
        assertEquals(1, result.getErrors().size());
        assertEquals("Schedule Error", result.getErrors().get(0).getErrorMessage());
    }

    @Test
    void ensureImportRowWithFewerColumnsRecordsError() {
        String csvContent = "flightNumber,routeOrigin,routeDestination,aircraftRegistration,departureDate,arrivalDate,status\n" +
                            "TP100,LIS,OPO";
        MockMultipartFile file = new MockMultipartFile("file", "fewer_columns.csv", "text/csv", csvContent.getBytes());

        BulkImportResult<String> result = importFlightsUseCase.execute(file);

        assertEquals(0, result.getSuccessfulImports().size());
        assertEquals(1, result.getErrors().size());
    }

    @Test
    void ensureImportFileStreamErrorRecordsError() throws Exception {
        org.springframework.web.multipart.MultipartFile mockFile = mock(org.springframework.web.multipart.MultipartFile.class);
        when(mockFile.getInputStream()).thenThrow(new java.io.IOException("Stream failure"));

        BulkImportResult<String> result = importFlightsUseCase.execute(mockFile);

        assertEquals(0, result.getSuccessfulImports().size());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains("Stream failure"));
    }

    @Test
    void ensureImportMissingOtherRequiredHeadersReturnsError() {
        // Missing destIndex
        String csv1 = "flightNumber,routeOrigin,aircraftRegistration,departureDate,arrivalDate\nTP100,LIS,CS-TVA,2026-06-19T10:00:00Z,2026-06-19T11:00:00Z";
        assertTrue(importFlightsUseCase.execute(new MockMultipartFile("file", "flights.csv", "text/csv", csv1.getBytes())).getErrors().get(0).getErrorMessage().contains("Missing required columns"));

        // Missing aircraftIndex
        String csv2 = "flightNumber,routeOrigin,routeDestination,departureDate,arrivalDate\nTP100,LIS,OPO,2026-06-19T10:00:00Z,2026-06-19T11:00:00Z";
        assertTrue(importFlightsUseCase.execute(new MockMultipartFile("file", "flights.csv", "text/csv", csv2.getBytes())).getErrors().get(0).getErrorMessage().contains("Missing required columns"));

        // Missing depIndex
        String csv3 = "flightNumber,routeOrigin,routeDestination,aircraftRegistration,arrivalDate\nTP100,LIS,OPO,CS-TVA,2026-06-19T11:00:00Z";
        assertTrue(importFlightsUseCase.execute(new MockMultipartFile("file", "flights.csv", "text/csv", csv3.getBytes())).getErrors().get(0).getErrorMessage().contains("Missing required columns"));

        // Missing arrIndex
        String csv4 = "flightNumber,routeOrigin,routeDestination,aircraftRegistration,departureDate\nTP100,LIS,OPO,CS-TVA,2026-06-19T10:00:00Z";
        assertTrue(importFlightsUseCase.execute(new MockMultipartFile("file", "flights.csv", "text/csv", csv4.getBytes())).getErrors().get(0).getErrorMessage().contains("Missing required columns"));
    }

    @Test
    void ensureImportWithoutFlightNumberHeaderReturnsDefaultIdentifier() {
        // Omit flightNumber column from header
        String csvContent = "routeOrigin,routeDestination,aircraftRegistration,departureDate,arrivalDate,status\n" +
                            "LIS,OPO,CS-TVA,2026-06-19T10:00:00Z,2026-06-19T11:00:00Z,SCHEDULED";
        MockMultipartFile file = new MockMultipartFile("file", "no_header.csv", "text/csv", csvContent.getBytes());

        BulkImportResult<String> result = importFlightsUseCase.execute(file);

        assertEquals(1, result.getSuccessfulImports().size());
        assertEquals("CS-TVA-LIS-OPO", result.getSuccessfulImports().get(0));
    }

    @Test
    void ensureImportAdditionalCoverage() {
        // Missing originIndex
        String csv1 = "flightNumber,routeDestination,aircraftRegistration,departureDate,arrivalDate\nTP100,OPO,CS-TVA,2026-06-19T10:00:00Z,2026-06-19T11:00:00Z";
        assertTrue(importFlightsUseCase.execute(new MockMultipartFile("file", "flights.csv", "text/csv", csv1.getBytes())).getErrors().get(0).getErrorMessage().contains("Missing required columns"));

        // departureDate is valid, arrivalDate is invalid
        String csv2 = "flightNumber,routeOrigin,routeDestination,aircraftRegistration,departureDate,arrivalDate,status\n" +
                      "TP100,LIS,OPO,CS-TVA,2026-06-19T10:00:00Z,invalid-date,SCHEDULED";
        assertTrue(importFlightsUseCase.execute(new MockMultipartFile("file", "flights.csv", "text/csv", csv2.getBytes())).getErrors().get(0).getErrorMessage().contains("Invalid date format"));

        // departureDate is invalid, arrivalDate is valid
        String csv3 = "flightNumber,routeOrigin,routeDestination,aircraftRegistration,departureDate,arrivalDate,status\n" +
                      "TP100,LIS,OPO,CS-TVA,invalid-date,2026-06-19T11:00:00Z,SCHEDULED";
        assertTrue(importFlightsUseCase.execute(new MockMultipartFile("file", "flights.csv", "text/csv", csv3.getBytes())).getErrors().get(0).getErrorMessage().contains("Invalid date format"));

        // flightNumber is empty string ("")
        String csv4 = "flightNumber,routeOrigin,routeDestination,aircraftRegistration,departureDate,arrivalDate,status\n" +
                      "   ,LIS,OPO,CS-TVA,2026-06-19T10:00:00Z,2026-06-19T11:00:00Z,SCHEDULED";
        assertEquals("CS-TVA-LIS-OPO", importFlightsUseCase.execute(new MockMultipartFile("file", "flights.csv", "text/csv", csv4.getBytes())).getSuccessfulImports().get(0));
    }
}
