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
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private ViewAircraftDetailsResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = new ViewAircraftDetailsResponse(
                "CS-TPA", "A320", Manufacturer.AIRBUS, LocalDate.of(2020, 1, 1),
                AircraftStatus.AVAILABLE, 150, List.of("WiFi"), 0L);
    }

    @Test
    void ensureRegisterAircraftReturns201() throws Exception {
        RegisterAircraftRequest request = new RegisterAircraftRequest(
                "CS-TPA", "A320", LocalDate.of(2020, 1, 1), 150, "AVAILABLE", List.of());

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
        UpdateAircraftRequest request = new UpdateAircraftRequest("A321", null, 160, null);

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
        when(listAircraft.execute(anyInt(), anyInt())).thenReturn(new aisafe.shared.application.dtos.PaginatedResult<>(List.of(), 0L));

        mockMvc.perform(get("/api/aircrafts"))
                .andExpect(status().isOk());
    }

    @Test
    void ensureSearchAircraftReturns200() throws Exception {
        when(searchAircraft.execute(any(), any(), any(), anyInt(), anyInt())).thenReturn(new aisafe.shared.application.dtos.PaginatedResult<>(List.of(), 0L));

        mockMvc.perform(get("/api/aircrafts/search")
                        .param("modelName", "A320"))
                .andExpect(status().isOk());
    }
}
