package aisafe.aircrafts.infrastructure;

import aisafe.aircrafts.application.*;
import aisafe.aircrafts.application.dtos.*;
import aisafe.aircrafts.domain.AircraftInvalidFieldException;
import aisafe.aircrafts.domain.AircraftNotFoundException;
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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
    private ViewFleetStatusUseCase viewFleetStatus;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ImportAircraftsUseCase importAircrafts;

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
    void ensureRegisterAircraftReturns201() throws Exception {
        RegisterAircraftRequest request = new RegisterAircraftRequest(
                "CS-TPA", "A320", LocalDate.of(2020, 1, 1), 150, 5000.0, "AVAILABLE", List.of());

        when(registerAircraft.execute(any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/aircrafts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registrationNumber").value("CS-TPA"));
    }

    @Test
    void ensureUpdateAircraftReturns200() throws Exception {
        UpdateAircraftRequest request = new UpdateAircraftRequest("A321", null, 160, 5000.0, null, "INACTIVE");

        when(updateAircraftUseCase.execute(any(), any(), any())).thenReturn(sampleResponse);

        mockMvc.perform(patch("/api/aircrafts/CS-TPA")
                        .header("If-Match", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void ensureUpdateAircraftReturns409OnConcurrencyError() throws Exception {
        UpdateAircraftRequest request = new UpdateAircraftRequest("A321", null, null, null, null, null);

        when(updateAircraftUseCase.execute(any(), any(), any()))
                .thenThrow(new ObjectOptimisticLockingFailureException(aisafe.aircrafts.domain.Aircraft.class, "CS-TPA"));

        mockMvc.perform(patch("/api/aircrafts/CS-TPA")
                        .header("If-Match", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void ensureGetAircraftReturns404WhenNotFound() throws Exception {
        when(viewAircraftDetails.execute(any())).thenThrow(new AircraftNotFoundException("Not found"));

        mockMvc.perform(get("/api/aircrafts/CS-UNK"))
                .andExpect(status().isNotFound());
    }

    @Test
    void ensureUpdateAircraftReturns400OnInvalidData() throws Exception {
        UpdateAircraftRequest request = new UpdateAircraftRequest(null, null, -10, null, null, null);

        when(updateAircraftUseCase.execute(any(), any(), any()))
                .thenThrow(new AircraftInvalidFieldException("Invalid capacity"));

        mockMvc.perform(patch("/api/aircrafts/CS-TPA")
                        .header("If-Match", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureSearchAircraftReturns200() throws Exception {
        when(searchAircraft.execute(any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(new PaginatedResult<>(List.of(), 0L));

        mockMvc.perform(get("/api/aircrafts/search")
                        .param("modelName", "A320"))
                .andExpect(status().isOk());
    }

    @Test
    void ensureGetFleetStatusReturns200() throws Exception {
        FleetStatusGroupResponse availableGroup = new FleetStatusGroupResponse(
                AircraftStatus.AVAILABLE,
                new PaginatedResult<>(List.of(
                        new FleetStatusAircraftResponse("CS-TPA", "A320", Manufacturer.AIRBUS)
                ), 1));
        FleetStatusGroupResponse maintenanceGroup = new FleetStatusGroupResponse(
                AircraftStatus.UNDER_MAINTENANCE, new PaginatedResult<>(List.of(), 0));
        FleetStatusGroupResponse inFlightGroup = new FleetStatusGroupResponse(
                AircraftStatus.IN_FLIGHT, new PaginatedResult<>(List.of(), 0));
        FleetStatusGroupResponse inactiveGroup = new FleetStatusGroupResponse(
                AircraftStatus.INACTIVE, new PaginatedResult<>(List.of(), 0));

        FleetStatusResponse fleetResponse = new FleetStatusResponse(1,
                new PaginatedResult<>(List.of(availableGroup, maintenanceGroup, inFlightGroup, inactiveGroup), 4));

        when(viewFleetStatus.execute()).thenReturn(fleetResponse);

        mockMvc.perform(get("/api/aircrafts/fleet-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAircraft").value(1))
                .andExpect(jsonPath("$.statusGroups.totalElements").value(4));
    }

    @Test
    void ensureImportAircraftsReturns207WhenPartialSuccess() throws Exception {
        aisafe.shared.application.dtos.BulkImportResult<ViewAircraftDetailsResponse> result = new aisafe.shared.application.dtos.BulkImportResult<>();
        result.addSuccess(sampleResponse);
        result.addError(2, "data", "error");

        when(importAircrafts.execute(any())).thenReturn(result);

        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile("file", "test.csv", "text/csv", "dummy".getBytes());

        mockMvc.perform(multipart("/api/aircrafts/import").file(file))
                .andExpect(status().isMultiStatus())
                .andExpect(jsonPath("$.successfulCount").value(1))
                .andExpect(jsonPath("$.errorCount").value(1));
    }
}
