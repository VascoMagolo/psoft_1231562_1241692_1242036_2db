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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
    private UpdateMaintenancePartUseCase updateMaintenancePartUseCase;

    @MockitoBean
    private UpdateMaintenanceTemplateUseCase updateMaintenanceTemplateUseCase;

    @MockitoBean
    private SearchMaintenancePartUseCase searchMaintenancePartUseCase;

    @MockitoBean
    private SearchMaintenanceRecordsUseCase searchMaintenanceRecordsUseCase;

    @MockitoBean
    private ViewOngoingMaintenanceUseCase viewOngoingMaintenanceUseCase;

    @MockitoBean
    private ViewMaintenanceCostByAircraftUseCase viewMaintenanceCostByAircraftUseCase;

    @MockitoBean
    private ViewMaintenanceCostByModelUseCase viewMaintenanceCostByModelUseCase;

    @MockitoBean
    private ViewAverageMaintenanceTurnaroundUseCase viewAverageTurnaroundUseCase;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private UUID sampleRecordId;
    private MaintenanceRecordResponse sampleRecordResponse;

    @BeforeEach
    void setUp() {
        sampleRecordId = UUID.randomUUID();
        sampleRecordResponse = new MaintenanceRecordResponse(
                sampleRecordId, "Engine inspection", LocalDateTime.of(2026, 5, 23, 10, 0),
                4, null, List.of("P001"), "Annual Check", "PLANNED", "CS-TPA", 0L, Set.of("ENGINE"), BigDecimal.valueOf(500));
    }

    @Test
    void ensureUpdateTemplateReturns200() throws Exception {
        UpdateMaintenanceTemplateRequest request = new UpdateMaintenanceTemplateRequest(List.of("Check engine"), 150, 45);

        when(updateMaintenanceTemplateUseCase.execute(any(), any())).thenReturn(new MaintenanceTemplateResponse("Annual Check", "INSPECTION"));

        mockMvc.perform(patch("/api/maintenance/templates/Annual Check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Annual Check"));
    }

    @Test
    void ensureUpdatePartReturns200() throws Exception {
        UpdateMaintenancePartRequest request = new UpdateMaintenancePartRequest("New description", 20, 10);

        when(updateMaintenancePartUseCase.execute(any(), any())).thenReturn(new MaintenancePartResponse("P001", "Engine Filter", "New description", 20, 10, MaintenanceComponent.ENGINE));

        mockMvc.perform(patch("/api/maintenance/parts/P001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partNumber").value("P001"));
    }

    @Test
    void ensureCreateTemplateReturns201() throws Exception {
        CreateMaintenanceTemplateRequest request = new CreateMaintenanceTemplateRequest(
                "Annual Check", MaintenanceType.INSPECTION, List.of("A320"), List.of("Check engine"), 500, 365);

        when(createMaintenanceTemplateUseCase.execute(any())).thenReturn(new MaintenanceTemplateResponse("Annual Check", null));

        mockMvc.perform(post("/api/maintenance/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void ensureCreatePartReturns201() throws Exception {
        CreateMaintenancePartRequest request = new CreateMaintenancePartRequest(
                "P001", "Engine Filter", null, 10, 2, MaintenanceComponent.ENGINE);

        when(createMaintenancePartUseCase.execute(any())).thenReturn(new MaintenancePartResponse("P001", "Engine Filter", null, 10, 2, MaintenanceComponent.ENGINE));

        mockMvc.perform(post("/api/maintenance/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void ensureSearchPartsReturns200() throws Exception {
        MaintenancePartResponse part = new MaintenancePartResponse("P001", "Engine Filter", "Desc", 10, 2, MaintenanceComponent.ENGINE);
        when(searchMaintenancePartUseCase.execute(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PaginatedResult<>(List.of(part), 1));

        mockMvc.perform(get("/api/maintenance/parts/search?name=Engine&lowStock=false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.maintenancePartResponseList[0].partNumber").value("P001"))
                .andExpect(jsonPath("$._embedded.maintenancePartResponseList[0]._links.update").exists());
    }

    @Test
    void ensureCreateRecordReturns201() throws Exception {
        CreateMaintenanceRecordRequest request = new CreateMaintenanceRecordRequest(
                "Engine inspection", LocalDateTime.of(2026, 5, 23, 10, 0),
                4, List.of("P001"), null, "Annual Check", MaintenanceStatus.PLANNED, "CS-TPA", Set.of(MaintenanceComponent.ENGINE), BigDecimal.valueOf(500));

        when(createMaintenanceRecordUseCase.execute(any())).thenReturn(sampleRecordResponse);

        mockMvc.perform(post("/api/maintenance/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Engine inspection"))
                .andExpect(jsonPath("$.components").isArray())
                .andExpect(jsonPath("$._links.update-record").exists());
    }

    @Test
    void ensureUpdateRecordWithIfMatchReturns200() throws Exception {
        UpdateMaintenanceRecordsRequest request = new UpdateMaintenanceRecordsRequest(MaintenanceStatus.IN_PROGRESS, "Updated notes");

        when(updateMaintenanceRecordUseCase.execute(any(UUID.class), any(), any())).thenReturn(sampleRecordResponse);

        mockMvc.perform(patch("/api/maintenance/records/" + sampleRecordId)
                        .header("If-Match", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partNumbers[0]").value("P001"));
    }

    @Test
    void ensureUpdateRecordWithoutIfMatchReturns400() throws Exception {
        UpdateMaintenanceRecordsRequest request = new UpdateMaintenanceRecordsRequest(MaintenanceStatus.IN_PROGRESS, null);

        mockMvc.perform(patch("/api/maintenance/records/" + sampleRecordId)
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

    @Test
    void ensureSearchRecordsReturns200WithNoFilters() throws Exception {
        when(searchMaintenanceRecordsUseCase.execute(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PaginatedResult<>(List.of(), 0));

        mockMvc.perform(get("/api/maintenance/records/search"))
                .andExpect(status().isOk());
    }

    @Test
    void ensureSearchRecordsReturns200WithAllFilters() throws Exception {
        when(searchMaintenanceRecordsUseCase.execute(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PaginatedResult<>(List.of(), 0));

        mockMvc.perform(get("/api/maintenance/records/search")
                        .param("registration", "CS-TPA")
                        .param("from", "2026-01-01T00:00:00")
                        .param("to", "2026-12-31T23:59:59")
                        .param("component", "ENGINE"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MAINTENANCE_TECHNICIAN")
    void ensureMaintenanceTechnicianCanSearch() throws Exception {
        when(searchMaintenanceRecordsUseCase.execute(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PaginatedResult<>(List.of(), 0));

        mockMvc.perform(get("/api/maintenance/records/search"))
                .andExpect(status().isOk());
    }

    @Test
    void ensureSearchRecordsReturns400ForInvalidComponent() throws Exception {
        mockMvc.perform(get("/api/maintenance/records/search")
                        .param("component", "INVALID_COMPONENT"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureGetOngoingMaintenanceReturns200() throws Exception {
        when(viewOngoingMaintenanceUseCase.execute(anyInt(), anyInt()))
                .thenReturn(new PaginatedResult<>(List.of(), 0));

        mockMvc.perform(get("/api/maintenance/records/ongoing"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MAINTENANCE_SUPERVISOR")
    void ensureMaintenanceSupervisorCanViewOngoing() throws Exception {
        when(viewOngoingMaintenanceUseCase.execute(anyInt(), anyInt()))
                .thenReturn(new PaginatedResult<>(List.of(), 0));

        mockMvc.perform(get("/api/maintenance/records/ongoing"))
                .andExpect(status().isOk());
    }

    @Test
    void ensureGetCostByAircraftReturns200() throws Exception {
        when(viewMaintenanceCostByAircraftUseCase.execute("CS-TPA"))
                .thenReturn(new MaintenanceCostByAircraftResponse("CS-TPA", BigDecimal.valueOf(1500)));

        mockMvc.perform(get("/api/maintenance/records/cost/aircraft/CS-TPA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aircraftRegistration").value("CS-TPA"))
                .andExpect(jsonPath("$.totalCost").value(1500));
    }

    @Test
    @WithMockUser(roles = "ATCC")
    void ensureAtccCanGetCostByAircraft() throws Exception {
        when(viewMaintenanceCostByAircraftUseCase.execute("CS-TPA"))
                .thenReturn(new MaintenanceCostByAircraftResponse("CS-TPA", BigDecimal.valueOf(1500)));

        mockMvc.perform(get("/api/maintenance/records/cost/aircraft/CS-TPA"))
                .andExpect(status().isOk());
    }

    @Test
    void ensureGetCostByModelReturns200() throws Exception {
        when(viewMaintenanceCostByModelUseCase.execute("A320"))
                .thenReturn(new MaintenanceCostByModelResponse("A320", BigDecimal.valueOf(4250)));

        mockMvc.perform(get("/api/maintenance/records/cost/model/A320"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelName").value("A320"))
                .andExpect(jsonPath("$.totalCost").value(4250));
    }

    @Test
    @WithMockUser(roles = "ATCC")
    void ensureAtccCanGetCostByModel() throws Exception {
        when(viewMaintenanceCostByModelUseCase.execute("A320"))
                .thenReturn(new MaintenanceCostByModelResponse("A320", BigDecimal.valueOf(4250)));

        mockMvc.perform(get("/api/maintenance/records/cost/model/A320"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MAINTENANCE_SUPERVISOR")
    void ensureMaintenanceSupervisorCanViewAverageTurnaround() throws Exception {
        when(viewAverageTurnaroundUseCase.execute("Airbus A320neo"))
                .thenReturn(new AverageTurnaroundByModelResponse("Airbus A320neo", 32.0));

        mockMvc.perform(get("/api/maintenance/records/turnaround/model/Airbus A320neo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelName").value("Airbus A320neo"))
                .andExpect(jsonPath("$.averageHours").value(32.0));
    }
}
