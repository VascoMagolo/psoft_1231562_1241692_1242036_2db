package aisafe.routes.infrastructure;

import aisafe.routes.application.*;
import aisafe.routes.application.dtos.CreateRouteRequest;
import aisafe.routes.domain.Route;
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
import static org.mockito.ArgumentMatchers.anyLong;
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
    private DesactivateRouteUseCase desactivateRoute;

    @MockitoBean
    private ViewRouteDetailsUseCase viewRouteDetails;

    @MockitoBean
    private ListRoutesFromAirportUseCase listRoutesFromAirport;

    @MockitoBean
    private SearchRoutesUseCase searchRoutes;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private Route sampleRoute;

    @BeforeEach
    void setUp() {
        sampleRoute = new Route("OPO", "LIS", 45, 300.0, 150);
    }

    @Test
    void ensureCreateRouteReturns201() throws Exception {
        CreateRouteRequest request = new CreateRouteRequest("OPO", "LIS", 45, 300.0, 150);

        when(createRoute.execute(any())).thenReturn(sampleRoute);

        mockMvc.perform(post("/api/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.origin.code").value("OPO"))
                .andExpect(jsonPath("$.destination.code").value("LIS"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void ensureGetRouteByIdReturns200() throws Exception {
        when(viewRouteDetails.execute(anyLong())).thenReturn(sampleRoute);

        mockMvc.perform(get("/api/routes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.origin.code").value("OPO"));
    }

    @Test
    void ensureDeactivateRouteReturns200() throws Exception {
        Route deactivated = new Route("OPO", "LIS", 45, 300.0, 150);
        deactivated.deactivate();
        when(desactivateRoute.execute(anyLong())).thenReturn(deactivated);

        mockMvc.perform(patch("/api/routes/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void ensureGetRoutesFromAirportReturns200() throws Exception {
        when(listRoutesFromAirport.execute(anyString())).thenReturn(List.of(sampleRoute));

        mockMvc.perform(get("/api/routes/airport/OPO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].origin.code").value("OPO"));
    }
}
