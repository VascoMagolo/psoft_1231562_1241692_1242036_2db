package aisafe.routes.infrastructure;

import aisafe.airports.domain.*;
import aisafe.routes.application.*;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import aisafe.routes.infrastructure.serialization.GeoJsonRouteNetworkSerializer;
import aisafe.routes.infrastructure.serialization.KmlRouteNetworkSerializer;
import aisafe.security.application.JwtService;
import aisafe.security.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RouteController.class)
@Import({ExportRouteNetworkUseCase.class, GeoJsonRouteNetworkSerializer.class, KmlRouteNetworkSerializer.class})
@AutoConfigureMockMvc(addFilters = false)
class RouteControllerExportIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RouteRepository routeRepository;

    @MockitoBean
    private AirportRepository airportRepository;

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
    private ListActiveRoutesUseCase listActiveRoutes;
    @MockitoBean
    private SearchAlternativeRoutesUseCase searchAlternativeRoutes;

    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        Route route = new Route("OPO", "LIS", 60, 300.0, 100);
        when(routeRepository.findAllActive()).thenReturn(List.of(route));

        Airport origin = new Airport("OPO", "Francisco Sa Carneiro", "Porto", "Portugal", "North", "GMT", 41.2481, -8.6814, List.of(new Runway("RW1", 3000, "Asphalt")));
        Airport destination = new Airport("LIS", "Humberto Delgado", "Lisbon", "Portugal", "Center", "GMT", 38.7742, -9.1342, List.of(new Runway("RW2", 3800, "Concrete")));

        when(airportRepository.findByIataCode(new IataCode("OPO"))).thenReturn(Optional.of(origin));
        when(airportRepository.findByIataCode(new IataCode("LIS"))).thenReturn(Optional.of(destination));
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void ensureGeoJsonExportWorks() throws Exception {
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
        mockMvc.perform(get("/api/routes/export")
                        .param("format", "kml"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/vnd.google-earth.kml+xml"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"routes.kml\""))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Placemark")));
    }
}
