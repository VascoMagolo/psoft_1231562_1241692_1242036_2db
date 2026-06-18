package aisafe.flights.infrastructure;

import aisafe.flights.application.GenerateFlightUtilizationReportUseCase;
import aisafe.flights.application.dtos.RouteUtilizationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GenerateFlightUtilizationReportUseCase useCase;

    @MockitoBean
    private aisafe.security.application.JwtService jwtService;

    @MockitoBean
    private aisafe.security.domain.UserRepository userRepository;

    @Test
    @WithMockUser(roles = "OPERATOR")
    void ensureCanGetFlightUtilizationReport() throws Exception {
        RouteUtilizationResponse data = new RouteUtilizationResponse(1L, "OPO", "LIS", 15L);
        when(useCase.execute(any(), any(), eq(0), eq(20))).thenReturn(List.of(data));

        mockMvc.perform(get("/api/reports/flight-utilization")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].routeId").value(1))
                .andExpect(jsonPath("$[0].origin").value("OPO"))
                .andExpect(jsonPath("$[0].destination").value("LIS"))
                .andExpect(jsonPath("$[0].count").value(15));
    }
}
