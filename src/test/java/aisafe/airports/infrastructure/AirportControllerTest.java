package aisafe.airports.infrastructure;

import aisafe.airports.application.*;
import aisafe.airports.application.dtos.*;
import aisafe.airports.domain.AirportStatus;
import aisafe.security.application.JwtService;
import aisafe.security.domain.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AirportController.class)
@AutoConfigureMockMvc(addFilters = false)
class AirportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private RegisterAirportUseCase registerAirport;

    @MockitoBean
    private AddAirportCertificationUseCase addCertification;

    @MockitoBean
    private ViewAirportDetailsUseCase viewAirportDetails;

    @MockitoBean
    private SearchAirportUseCase searchAirport;

    @MockitoBean
    private UpdateAirportStatusUseCase updateAirportStatus;

    @MockitoBean
    private UpdateAirportDetailsUseCase updateAirportDetails;

    @MockitoBean
    private ViewAirportRoutesUseCase viewAirportRoutes;

    @MockitoBean
    private AirportStatisticsUseCase airportStatistics;

    @MockitoBean
    private ListAirportsByRegionUseCase listAirportsByRegion;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private AirportResponse sampleAirportResponse;

    @BeforeEach
    void setUp() {
        sampleAirportResponse = new AirportResponse(
                1L, "LIS", "Lisbon Airport", "Lisbon", "Portugal", "Europe",
                "Europe/Lisbon", null, null, "OPERATIONAL",
                new AirportResponse.CoordinatesRecord(38.77, -9.13),
                List.of(new AirportResponse.RunwayRecord("03/21", 3000, "030/210")),
                List.of(), List.of(), List.of(), List.of());
    }

    @Test
    void ensureRegisterAirportReturns201() throws Exception {
        RegisterAirportRequest request = new RegisterAirportRequest(
                "LIS", "Lisbon Airport", "Lisbon", "Portugal", "Europe", "Europe/Lisbon",
                38.77, -9.13,
                List.of(new RegisterAirportRequest.RunwayRequest("03/21", 3000, "030/210")),
                null, null, null, null, null);

        when(registerAirport.execute(any())).thenReturn(sampleAirportResponse);

        mockMvc.perform(post("/api/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.iataCode").value("LIS"))
                .andExpect(jsonPath("$._links").exists());
    }

    @Test
    void ensureRegisterAirportWithMissingRunwayReturns400() throws Exception {
        RegisterAirportRequest request = new RegisterAirportRequest(
                "LIS", "Lisbon Airport", "Lisbon", "Portugal", "Europe", "Europe/Lisbon",
                38.77, -9.13,
                List.of(),
                null, null, null, null, null);

        mockMvc.perform(post("/api/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureGetAirportByIataCodeReturns200() throws Exception {
        when(viewAirportDetails.execute(anyString())).thenReturn(sampleAirportResponse);

        mockMvc.perform(get("/api/airports/LIS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iataCode").value("LIS"))
                .andExpect(jsonPath("$.status").value("OPERATIONAL"));
    }

    @Test
    void ensureUpdateAirportStatusReturns200() throws Exception {
        UpdateAirportStatusRequest request = new UpdateAirportStatusRequest(AirportStatus.CLOSED);

        when(updateAirportStatus.execute(anyString(), any())).thenReturn(sampleAirportResponse);

        mockMvc.perform(patch("/api/airports/LIS/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iataCode").value("LIS"));
    }
}
