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
    void shouldImportValidFlights() throws Exception {
        String csvContent = "flightNumber,routeOrigin,routeDestination,aircraftRegistration,departureDate,arrivalDate,status\n" +
                            "TP100,LIS,OPO,CS-TVA,2026-06-19T10:00:00Z,2026-06-19T11:00:00Z,SCHEDULED";
        MockMultipartFile file = new MockMultipartFile("file", "flights.csv", "text/csv", csvContent.getBytes());

        BulkImportResult<String> result = importFlightsUseCase.execute(file);

        assertEquals(1, result.getSuccessfulImports().size());
        assertTrue(result.getErrors().isEmpty());
        verify(scheduleFlightUseCase, times(1)).execute(any());
    }
}
