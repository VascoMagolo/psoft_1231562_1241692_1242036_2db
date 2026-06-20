package aisafe.routes.infrastructure;

import aisafe.routes.application.*;
import aisafe.routes.application.dtos.CreateRouteRequest;
import aisafe.routes.application.dtos.RouteResponse;
import aisafe.routes.application.dtos.UpdateRouteRequest;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import aisafe.routes.domain.RouteStatus;
import aisafe.security.application.JwtService;
import aisafe.security.domain.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import aisafe.shared.domain.PaginatedResult;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RouteController.class)
@AutoConfigureMockMvc(addFilters = false)
class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private CreateRouteUseCase createRoute;

    @MockitoBean
    private ViewRouteHistoryUseCase viewRouteHistory;

    @MockitoBean
    private UpdateRouteUseCase updateRoute;

    @MockitoBean
    private DeactivateRouteUseCase deactivateRoute;

    @MockitoBean
    private ViewRouteDetailsUseCase viewRouteDetails;

    @MockitoBean
    private ListRoutesFromAirportUseCase listRoutesFromAirport;

    @MockitoBean
    private SearchRoutesUseCase searchRoutes;

    @MockitoBean
    private DeleteRouteUseCase deleteRoute;

    @MockitoBean
    private RouteRepository routeRepository;

    @MockitoBean
    private ListActiveRoutesUseCase listActiveRoutes;

    @MockitoBean
    private SearchAlternativeRoutesUseCase searchAlternativeRoutes;

    @MockitoBean
    private ExportRouteNetworkUseCase exportRouteNetwork;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private ImportRoutesUseCase importRoutesUseCase;

    private Route sampleRoute;

    @BeforeEach
    void setUp() {
        sampleRoute = new Route("OPO", "LIS", 45, 300.0, 150);
    }

    @Test
    void ensureCreateRouteReturns201() throws Exception {
        CreateRouteRequest request = new CreateRouteRequest("OPO", "LIS", 45, 300.0, 150, "admin");

        when(createRoute.execute(any())).thenReturn(RouteResponse.from(sampleRoute));

        mockMvc.perform(post("/api/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originIataCode").value("OPO"))
                .andExpect(jsonPath("$.destinationIataCode").value("LIS"))
                .andExpect(jsonPath("$.status").value(RouteStatus.ACTIVE.name()));
    }

    @Test
    void ensureGetRouteByOriginDestinationReturns200() throws Exception {
        when(viewRouteDetails.execute(anyString(), anyString())).thenReturn(RouteResponse.from(sampleRoute));

        mockMvc.perform(get("/api/routes/OPO/LIS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originIataCode").value("OPO"));
    }

    @Test
    void ensureDeactivateRouteReturns200() throws Exception {
        Route deactivated = new Route("OPO", "LIS", 45, 300.0, 150);
        deactivated.changeStatus(RouteStatus.INACTIVE);
        when(deactivateRoute.execute(anyString(), anyString(), any(), any())).thenReturn(RouteResponse.from(deactivated, 0L));

        mockMvc.perform(patch("/api/routes/OPO/LIS/deactivate")
                        .header("If-Match", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(RouteStatus.INACTIVE.name()));
    }

    @Test
    void ensureGetRoutesFromAirportReturns200() throws Exception {
        when(listRoutesFromAirport.execute(anyString(), anyInt(), anyInt()))
                .thenReturn(new PaginatedResult<>(List.of(RouteResponse.from(sampleRoute, 0L)), 1L));

        mockMvc.perform(get("/api/routes/airport/OPO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.routeResponseList[0].originIataCode").value("OPO"));
    }

    @Test
    void ensureGetActiveRoutesReturns200() throws Exception {
        when(listActiveRoutes.execute(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/routes")
                        .param("status", "active")
                        .param("sortBy", "distance"))
                .andExpect(status().isOk());
    }

    @Test
    void ensureGetAlternativeRoutesReturns200() throws Exception {
        when(searchAlternativeRoutes.execute(anyString(), anyString())).thenReturn(List.of());

        mockMvc.perform(get("/api/routes/alternatives")
                        .param("origin", "LIS")
                        .param("destination", "MAD"))
                .andExpect(status().isOk());
    }

    @Test
    void ensureGetRouteHistoryReturns200() throws Exception {
        when(viewRouteHistory.execute(anyString(), anyString())).thenReturn(List.of());

        mockMvc.perform(get("/api/routes/LIS/MAD/history"))
                .andExpect(status().isOk());
    }

    @Test
    void ensureUpdateRouteReturns200() throws Exception {
        UpdateRouteRequest request = new UpdateRouteRequest(50, 400.0, 200, true);
        when(updateRoute.execute(anyString(), anyString(), any(), any(), any())).thenReturn(RouteResponse.from(sampleRoute, 0L));

        mockMvc.perform(put("/api/routes/LIS/MAD")
                        .header("If-Match", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void ensureSearchRoutesReturns200() throws Exception {
        when(searchRoutes.execute(any()))
                .thenReturn(new PaginatedResult<>(List.of(RouteResponse.from(sampleRoute, 0L)), 1L));

        mockMvc.perform(get("/api/routes/search")
                        .param("origin", "LIS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.routeResponseList[0].originIataCode").value("OPO"));
    }

    @Test
    void ensureDeleteRouteReturns204() throws Exception {
        mockMvc.perform(delete("/api/routes/LIS/MAD"))
                .andExpect(status().isNoContent());
    }
}

