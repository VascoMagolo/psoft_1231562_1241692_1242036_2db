package aisafe.routes.infrastructure;

import aisafe.routes.application.*;
import aisafe.shared.application.ExportedFile;
import aisafe.security.application.JwtService;
import aisafe.security.domain.UserRepository;
import aisafe.security.infrastructure.JwtAuthenticationFilter;
import aisafe.security.infrastructure.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RouteController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@AutoConfigureMockMvc
class RouteControllerExportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExportRouteNetworkUseCase exportRouteNetwork;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private aisafe.routes.application.ImportRoutesUseCase importRoutesUseCase;


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
    private aisafe.routes.domain.RouteRepository routeRepository;
    @MockitoBean
    private ListActiveRoutesUseCase listActiveRoutes;
    @MockitoBean
    private SearchAlternativeRoutesUseCase searchAlternativeRoutes;

    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private org.springframework.security.authentication.AuthenticationProvider authenticationProvider;

    @Test
    @WithMockUser(roles = "BACKOFFICE_OPERATOR")
    void ensureGeoJsonExportWorksForBackofficeOperator() throws Exception {
        byte[] content = "{\"type\":\"FeatureCollection\"}".getBytes();
        when(exportRouteNetwork.execute("geojson"))
                .thenReturn(new ExportedFile(content, "application/geo+json", "routes.geojson"));

        mockMvc.perform(get("/api/routes/export")
                        .param("format", "geojson"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/geo+json"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"routes.geojson\""))
                .andExpect(jsonPath("$.type").value("FeatureCollection"));
    }

    @Test
    @WithMockUser(roles = "ATCC")
    void ensureExportIsForbiddenForAtcc() throws Exception {
        mockMvc.perform(get("/api/routes/export")
                        .param("format", "geojson"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void ensureKmlExportWorksForAdmin() throws Exception {
        byte[] content = "<Placemark></Placemark>".getBytes();
        when(exportRouteNetwork.execute("kml"))
                .thenReturn(new ExportedFile(content, "application/vnd.google-earth.kml+xml", "routes.kml"));

        mockMvc.perform(get("/api/routes/export")
                        .param("format", "kml"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/vnd.google-earth.kml+xml"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"routes.kml\""))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Placemark")));
    }
}

