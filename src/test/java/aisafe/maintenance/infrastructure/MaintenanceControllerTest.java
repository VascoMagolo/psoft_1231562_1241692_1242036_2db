package aisafe.maintenance.infrastructure;

import aisafe.maintenance.application.*;
import aisafe.maintenance.application.dtos.*;
import aisafe.maintenance.domain.*;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MaintenanceController.class)
@AutoConfigureMockMvc(addFilters = false)
class MaintenanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private CreateMaintenanceTemplateUseCase createMaintenanceTemplateUseCase;

    @MockitoBean
    private CreateMaintenanceRecordUseCase createMaintenanceRecordUseCase;

    @MockitoBean
    private CreateMaintenancePartUseCase createMaintenancePartUseCase;

    @MockitoBean
    private UpdateMaintenanceRecordUseCase updateMaintenanceRecordUseCase;

    @MockitoBean
    private ViewAllMaintenanceRecordsUseCase viewAllMaintenanceRecordsUseCase;

    @MockitoBean
    private ViewTotalMaintenanceHoursInFleetUseCase viewTotalMaintenanceHoursInFleetUseCase;

    @MockitoBean
    private DeleteMaintenanceRecordUseCase deleteMaintenanceRecordUseCase;

    @MockitoBean
    private DeleteMaintenanceTemplateUseCase deleteMaintenanceTemplateUseCase;

    @MockitoBean
    private DeleteMaintenancePartUseCase deleteMaintenancePartUseCase;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private MaintenanceRecordResponse sampleRecordResponse;

    @BeforeEach
    void setUp() {
        sampleRecordResponse = new MaintenanceRecordResponse(
                1L, "Engine inspection", LocalDateTime.of(2026, 5, 23, 10, 0),
                4, null, "P001", "Annual Check", "PLANNED", "CS-TPA", 0L);
    }

    @Test
    void ensureCreateTemplateReturns201() throws Exception {
        CreateMaintenanceTemplateRequest request = new CreateMaintenanceTemplateRequest(
                "Annual Check", MaintenanceType.INSPECTION, List.of("A320"), List.of("Check engine"), 500, 365);

        when(createMaintenanceTemplateUseCase.execute(any())).thenReturn(new MaintenanceTemplateResponse(1L, "Annual Check", null));

        mockMvc.perform(post("/api/maintenance/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void ensureCreatePartReturns201() throws Exception {
        CreateMaintenancePartRequest request = new CreateMaintenancePartRequest(
                "P001", "Engine Filter", null, 10, 2, MaintenanceComponent.ENGINE);

        when(createMaintenancePartUseCase.execute(any())).thenReturn(new MaintenancePartResponse(1L, "P001", null));

        mockMvc.perform(post("/api/maintenance/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void ensureCreateRecordReturns201() throws Exception {
        CreateMaintenanceRecordRequest request = new CreateMaintenanceRecordRequest(
                "Engine inspection", LocalDateTime.of(2026, 5, 23, 10, 0),
                4, "P001", null, "Annual Check", MaintenanceStatus.PLANNED, "CS-TPA");

        when(createMaintenanceRecordUseCase.execute(any())).thenReturn(sampleRecordResponse);

        mockMvc.perform(post("/api/maintenance/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Engine inspection"))
                .andExpect(jsonPath("$._links.update-record").exists());
    }

    @Test
    void ensureUpdateRecordWithIfMatchReturns200() throws Exception {
        UpdateMaintenanceRecordsRequest request = new UpdateMaintenanceRecordsRequest(MaintenanceStatus.IN_PROGRESS, "Updated notes");

        when(updateMaintenanceRecordUseCase.execute(anyLong(), any(), any())).thenReturn(sampleRecordResponse);

        mockMvc.perform(patch("/api/maintenance/records/1")
                        .header("If-Match", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partNumber").value("P001"));
    }

    @Test
    void ensureUpdateRecordWithoutIfMatchReturns400() throws Exception {
        UpdateMaintenanceRecordsRequest request = new UpdateMaintenanceRecordsRequest(MaintenanceStatus.IN_PROGRESS, null);

        mockMvc.perform(patch("/api/maintenance/records/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureGetTotalHoursReturns200() throws Exception {
        when(viewTotalMaintenanceHoursInFleetUseCase.execute()).thenReturn(new ViewTotalMaintenanceHoursInFleetResponse(120));

        mockMvc.perform(get("/api/maintenance/records/hours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalHours").value(120));
    }

    @Test
    void ensureGetRecordsByAircraftReturns200() throws Exception {
        when(viewAllMaintenanceRecordsUseCase.execute(any(), anyInt(), anyInt()))
                .thenReturn(new PaginatedResult<>(List.of(), 0));

        mockMvc.perform(get("/api/maintenance/records/aircraft/CS-TPA"))
                .andExpect(status().isOk());
    }
}
