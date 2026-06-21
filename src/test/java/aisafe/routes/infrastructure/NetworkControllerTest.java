package aisafe.routes.infrastructure;

import aisafe.routes.application.CalculateTotalNetworkDistanceUseCase;
import aisafe.routes.application.dtos.TotalDistanceResponse;
import aisafe.security.application.JwtService;
import aisafe.security.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NetworkController.class)
@AutoConfigureMockMvc(addFilters = false)
class NetworkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CalculateTotalNetworkDistanceUseCase calculateTotalNetworkDistance;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void ensureGetTotalDistanceReturns200() throws Exception {
        TotalDistanceResponse mockResponse = new TotalDistanceResponse(1250.5, "km");
        when(calculateTotalNetworkDistance.execute()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/network/total-distance")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDistance").value(1250.5))
                .andExpect(jsonPath("$.unit").value("km"));
    }
}
