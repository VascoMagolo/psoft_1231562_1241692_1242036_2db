package aisafe.routes.infrastructure;

import aisafe.routes.application.*;
import aisafe.shared.application.ExportedFile;
import aisafe.security.application.JwtService;
import aisafe.security.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RouteController.class)
@AutoConfigureMockMvc(addFilters = false)
class RouteControllerExportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExportRouteNetworkUseCase exportRouteNetwork;

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

    @Test
    @WithMockUser(roles = "OPERATOR")
    void ensureGeoJsonExportWorks() throws Exception {
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
    @WithMockUser(roles = "OPERATOR")
    void ensureKmlExportWorks() throws Exception {
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
