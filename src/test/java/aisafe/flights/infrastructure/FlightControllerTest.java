package aisafe.flights.infrastructure;

import aisafe.flights.application.ImportFlightsUseCase;
import aisafe.flights.application.ScheduleFlightUseCase;
import aisafe.flights.application.ViewScheduledFlightsByAircraftUseCase;
import aisafe.flights.application.dtos.FlightResponse;
import aisafe.flights.domain.FlightStatus;
import aisafe.security.application.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import aisafe.shared.application.dtos.BulkImportResult;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FlightController.class)
@AutoConfigureMockMvc(addFilters = false)
class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScheduleFlightUseCase scheduleFlight;

    @MockitoBean
    private ViewScheduledFlightsByAircraftUseCase viewScheduledFlightsByAircraft;

    @MockitoBean
    private ImportFlightsUseCase importFlightsUseCase;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser
    void scheduleFlightReturnsCreated() throws Exception {
        FlightResponse response = new FlightResponse(1L, "CS-TPA", "OPO", "LIS", OffsetDateTime.now(), OffsetDateTime.now().plusHours(2), FlightStatus.SCHEDULED);
        when(scheduleFlight.execute(any())).thenReturn(response);

        mockMvc.perform(post("/api/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aircraftId\":\"CS-TPA\",\"originIataCode\":\"OPO\",\"destinationIataCode\":\"LIS\",\"departureDateTime\":\"2026-06-15T10:00:00Z\",\"arrivalDateTime\":\"2026-06-15T12:00:00Z\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.aircraftId").value("CS-TPA"));
    }

    @Test
    @WithMockUser
    void getScheduledFlightsReturnsOk() throws Exception {
        FlightResponse response = new FlightResponse(1L, "CS-TPA", "OPO", "LIS", OffsetDateTime.now(), OffsetDateTime.now().plusHours(2), FlightStatus.SCHEDULED);
        when(viewScheduledFlightsByAircraft.execute("CS-TPA")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/flights?aircraftId=CS-TPA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.flightResponseList[0].aircraftId").value("CS-TPA"));
    }

    @Test
    void ensureImportFlightsSuccessReturns201() throws Exception {
        BulkImportResult<String> bulkImportResult = new BulkImportResult<>();
        bulkImportResult.addSuccess("FLIGHT-1");

        when(importFlightsUseCase.execute(any())).thenReturn(bulkImportResult);

        MockMultipartFile file = new MockMultipartFile(
                "file", "flights.csv", MediaType.TEXT_PLAIN_VALUE, "aircraft,route\nCS-TPA,OPO-LIS".getBytes());

        mockMvc.perform(multipart("/api/flights/import")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalProcessed").value(1))
                .andExpect(jsonPath("$.successfulCount").value(1))
                .andExpect(jsonPath("$.errorCount").value(0));
    }

    @Test
    void ensureImportFlightsPartialSuccessReturns207() throws Exception {
        BulkImportResult<String> bulkImportResult = new BulkImportResult<>();
        bulkImportResult.addSuccess("FLIGHT-1");
        bulkImportResult.addError(2, "bad-row", "invalid route");

        when(importFlightsUseCase.execute(any())).thenReturn(bulkImportResult);

        MockMultipartFile file = new MockMultipartFile(
                "file", "flights.csv", MediaType.TEXT_PLAIN_VALUE, "aircraft,route\nCS-TPA,OPO-LIS\nCS-TPB,invalid".getBytes());

        mockMvc.perform(multipart("/api/flights/import")
                        .file(file))
                .andExpect(status().isMultiStatus())
                .andExpect(jsonPath("$.totalProcessed").value(2))
                .andExpect(jsonPath("$.successfulCount").value(1))
                .andExpect(jsonPath("$.errorCount").value(1));
    }

    @Test
    void ensureImportFlightsFailureReturns400() throws Exception {
        BulkImportResult<String> bulkImportResult = new BulkImportResult<>();
        bulkImportResult.addError(1, "bad-row", "invalid route");

        when(importFlightsUseCase.execute(any())).thenReturn(bulkImportResult);

        MockMultipartFile file = new MockMultipartFile(
                "file", "flights.csv", MediaType.TEXT_PLAIN_VALUE, "aircraft,route\nCS-TPB,invalid".getBytes());

        mockMvc.perform(multipart("/api/flights/import")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.totalProcessed").value(1))
                .andExpect(jsonPath("$.successfulCount").value(0))
                .andExpect(jsonPath("$.errorCount").value(1));
    }
}

