package aisafe.flights.infrastructure;

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
}
