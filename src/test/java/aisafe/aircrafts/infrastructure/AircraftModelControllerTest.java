package aisafe.aircrafts.infrastructure;

import aisafe.aircrafts.application.*;
import aisafe.aircrafts.application.dtos.AircraftModelResponse;
import aisafe.aircrafts.application.dtos.RegisterAircraftModelRequest;
import aisafe.aircrafts.domain.Manufacturer;
import aisafe.security.application.JwtService;
import aisafe.security.domain.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AircraftModelController.class)
@AutoConfigureMockMvc(addFilters = false)
class AircraftModelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private RegisterAircraftModelUseCase registerAircraftModel;

    @MockitoBean
    private ListAircraftModelsUseCase listAircraftModels;

    @MockitoBean
    private DeleteAircraftModelUseCase deleteAircraftModel;

    @MockitoBean
    private UpdateAircraftModelUseCase updateAircraftModel;

    @MockitoBean
    private ViewAircraftModelDetailsUseCase viewAircraftModelDetails;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void ensureRegisterModelReturns201() throws Exception {
        RegisterAircraftModelRequest request = new RegisterAircraftModelRequest(
                "A320", Manufacturer.AIRBUS, 6150.0, 26730.0, 833.0, 180, "a320.jpg");

        AircraftModelResponse response = new AircraftModelResponse(
                 "A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "a320.jpg", 180);

        when(registerAircraftModel.execute(any())).thenReturn(response);

        mockMvc.perform(post("/api/aircraftModels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.modelName").value("A320"))
                .andExpect(jsonPath("$._links.all-models").exists());
    }

    @Test
    void ensureRegisterModelWithBlankNameReturns400() throws Exception {
        RegisterAircraftModelRequest request = new RegisterAircraftModelRequest(
                "", Manufacturer.AIRBUS, 6150.0, 26730.0, 833.0, 180, "a320.jpg");

        mockMvc.perform(post("/api/aircraftModels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureGetAllModelsReturns200() throws Exception {
        when(listAircraftModels.execute(anyInt(), anyInt())).thenReturn(List.of());

        mockMvc.perform(get("/api/aircraftModels"))
                .andExpect(status().isOk());
    }
}
