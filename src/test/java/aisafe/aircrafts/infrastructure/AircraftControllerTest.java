package aisafe.aircrafts.infrastructure;

import aisafe.aircrafts.application.*;
import aisafe.aircrafts.application.dtos.RegisterAircraftRequest;
import aisafe.aircrafts.application.dtos.UpdateAircraftRequest;
import aisafe.aircrafts.application.dtos.UpdateStatusRequest;
import aisafe.aircrafts.application.dtos.ViewAircraftDetailsResponse;
import aisafe.aircrafts.domain.AircraftStatus;
import aisafe.aircrafts.domain.Manufacturer;
import aisafe.security.application.JwtService;
import aisafe.security.domain.UserRepository;
import aisafe.shared.domain.PaginatedResult;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AircraftController.class)
@AutoConfigureMockMvc(addFilters = false)
class AircraftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private ViewAircraftDetailsUseCase viewAircraftDetails;

    @MockitoBean
    private ListAircraftUseCase listAircraft;

    @MockitoBean
    private RegisterAircraftUseCase registerAircraft;

    @MockitoBean
    private SearchAircraftUseCase searchAircraft;

    @MockitoBean
    private UpdateAircraftStatusUseCase updateAircraftStatus;

    @MockitoBean
    private DeleteAircraftUseCase deleteAircraft;

    @MockitoBean
    private UpdateAircraftUseCase updateAircraftUseCase;

    @MockitoBean
    private ViewCompatibleRoutesUseCase viewCompatibleRoutes;

    @MockitoBean
    private CalculateAircraftOperationalHoursUseCase calculateAircraftOperationalHours;

    @MockitoBean
    private GetAircraftUtilizationUseCase getAircraftUtilization;

    @MockitoBean
    private CalculateFuelEfficiencyUseCase calculateFuelEfficiency;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private ViewAircraftDetailsResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = new ViewAircraftDetailsResponse(
                "CS-TPA", "A320", Manufacturer.AIRBUS, LocalDate.of(2020, 1, 1),
                AircraftStatus.AVAILABLE, 150, 5000.0, List.of("WiFi"), 0L);
    }

    @Test
    void ensureGetFuelEfficiencyReturns200() throws Exception {
        when(calculateFuelEfficiency.execute(any(), any(), any())).thenReturn(
                new aisafe.aircrafts.application.dtos.FuelEfficiencyResponse("CS-TPA", 5.346, "OPO", "LIS", 2673.0)
        );

        mockMvc.perform(get("/api/aircrafts/CS-TPA/fuel-efficiency")
                        .param("origin", "OPO")
                        .param("destination", "LIS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fuelNeededForRoute").value(2673.0));
    }

    @Test
    void ensureGetAircraftUtilizationReturns200() throws Exception {
        when(getAircraftUtilization.execute(any(), any(), any())).thenReturn(List.of(
                new aisafe.aircrafts.application.dtos.UtilizationDataPointResponse(LocalDate.of(2023, 1, 1), 2.5, 10.4)
        ));

        mockMvc.perform(get("/api/aircrafts/CS-TPA/utilization")
                        .param("startDate", "2023-01-01")
                        .param("endDate", "2023-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.utilizationDataPointResponseList[0].date").value("2023-01-01"));
    }

    @Test
    void ensureGetOperationalHoursReturns200() throws Exception {
        when(calculateAircraftOperationalHours.execute(any())).thenReturn(
                new aisafe.aircrafts.application.dtos.AircraftOperationalHoursResponse("CS-TPA", 15.5));

        mockMvc.perform(get("/api/aircrafts/CS-TPA/operational-hours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber").value("CS-TPA"))
                .andExpect(jsonPath("$.totalOperationalHours").value(15.5));
    }

    @Test
    void ensureRegisterAircraftReturns201() throws Exception {
        RegisterAircraftRequest request = new RegisterAircraftRequest(
                "CS-TPA", "A320", LocalDate.of(2020, 1, 1), 150, 5000.0, "AVAILABLE", List.of());

        when(registerAircraft.execute(any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/aircrafts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registrationNumber").value("CS-TPA"))
                .andExpect(jsonPath("$._links").exists());
    }

    @Test
    void ensureGetAircraftByRegistrationReturns200() throws Exception {
        when(viewAircraftDetails.execute(any())).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/aircrafts/CS-TPA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber").value("CS-TPA"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void ensureUpdateStatusWithoutIfMatchHeaderReturns400() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest(AircraftStatus.INACTIVE);

        mockMvc.perform(patch("/api/aircrafts/CS-TPA/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureUpdateStatusWithIfMatchHeaderReturns200() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest(AircraftStatus.INACTIVE);

        when(updateAircraftStatus.execute(any(), any(), any())).thenReturn(sampleResponse);

        mockMvc.perform(patch("/api/aircrafts/CS-TPA/status")
                        .header("If-Match", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber").value("CS-TPA"));
    }

    @Test
    void ensureUpdateAircraftReturns200() throws Exception {
        UpdateAircraftRequest request = new UpdateAircraftRequest("A321", null, 160, 5000.0, null);

        when(updateAircraftUseCase.execute(any(), any(), any())).thenReturn(sampleResponse);

        mockMvc.perform(patch("/api/aircrafts/CS-TPA")
                        .header("If-Match", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber").value("CS-TPA"));
    }

    @Test
    void ensureDeleteAircraftReturns204() throws Exception {
        mockMvc.perform(delete("/api/aircrafts/CS-TPA"))
                .andExpect(status().isNoContent());
    }

    @Test
    void ensureListAircraftReturns200() throws Exception {
        when(listAircraft.execute(anyInt(), anyInt())).thenReturn(new PaginatedResult<>(List.of(), 0L));

        mockMvc.perform(get("/api/aircrafts"))
                .andExpect(status().isOk());
    }

    @Test
    void ensureSearchAircraftReturns200() throws Exception {
        when(searchAircraft.execute(any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(new PaginatedResult<>(List.of(), 0L));

        mockMvc.perform(get("/api/aircrafts/search")
                        .param("modelName", "A320"))
                .andExpect(status().isOk());
    }

    @Test
    void ensureSearchAircraftByFeatureReturns200() throws Exception {
        when(searchAircraft.execute(any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(new PaginatedResult<>(List.of(), 0L));

        mockMvc.perform(get("/api/aircrafts/search")
                        .param("feature", "WiFi"))
                .andExpect(status().isOk());
    }
}
