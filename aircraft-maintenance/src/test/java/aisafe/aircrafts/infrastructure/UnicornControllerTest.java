package aisafe.aircrafts.infrastructure;

import aisafe.aircraftmaintenance.infrastructure.SecurityConfig;
import aisafe.aircrafts.application.ViewAircraftMoodsUseCase;
import aisafe.aircrafts.application.dtos.AircraftMoodResponse;
import aisafe.shared.infrastructure.peers.PeerClient;
import aisafe.shared.infrastructure.security.JwtAuthenticationFilter;
import aisafe.shared.infrastructure.security.JwtTokenValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** {@code GET /api/v2/unicorns}: any signed-in role, never anonymous; says which side answered. */
@WebMvcTest(UnicornController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@AutoConfigureMockMvc
class UnicornControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ViewAircraftMoodsUseCase viewMoods;
    @MockitoBean private JwtTokenValidator tokenValidator;
    @MockitoBean private PeerClient peerClient;

    @Test
    @WithMockUser(roles = "MAINTENANCE_TECHNICIAN")
    void ensureAnySignedInRoleGetsTheMoods() throws Exception {
        when(viewMoods.execute()).thenReturn(List.of(
                new AircraftMoodResponse("CS-TKA", "Airbus A320neo", "AVAILABLE", "HAPPY", "Ready to fly")));

        mockMvc.perform(get("/api/v2/unicorns"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Query-Side", "peers"))
                .andExpect(jsonPath("$[0].registrationNumber").value("CS-TKA"))
                .andExpect(jsonPath("$[0].mood").value("HAPPY"));
    }

    @Test
    void ensureAnonymousIsRefused() throws Exception {
        // As every other route of the service without a token
        mockMvc.perform(get("/api/v2/unicorns")).andExpect(status().isForbidden());
    }
}
