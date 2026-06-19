package aisafe.aircrafts.infrastructure;

import aisafe.aircrafts.application.*;
import aisafe.aircrafts.application.dtos.AircraftModelImageData;
import aisafe.aircrafts.application.dtos.AircraftModelResponse;
import aisafe.aircrafts.application.dtos.RegisterAircraftModelRequest;
import aisafe.aircrafts.application.dtos.TopUtilizedModelResponse;
import aisafe.aircrafts.domain.AircraftModelImageNotFoundException;
import aisafe.aircrafts.domain.Manufacturer;
import aisafe.security.application.JwtService;
import aisafe.security.domain.UserRepository;
import aisafe.shared.application.dtos.BulkImportResult;
import aisafe.shared.domain.PaginatedResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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
    private GetTopUtilizedModelsUseCase getTopUtilizedModels;

    @MockitoBean
    private UpdateAircraftModelImageUseCase updateAircraftModelImage;

    @MockitoBean
    private GetAircraftModelImageUseCase getAircraftModelImage;

    @MockitoBean
    private ImportAircraftModelsUseCase importAircraftModels;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void ensureGetTopUtilizedModelsReturns200() throws Exception {
        when(getTopUtilizedModels.execute("HOURS")).thenReturn(List.of(new TopUtilizedModelResponse("A320", 5000L)));

        mockMvc.perform(get("/api/aircraftModels/top-utilized")
                        .param("criteria", "HOURS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.topUtilizedModelResponseList[0].modelName").value("A320"))
                .andExpect(jsonPath("$._embedded.topUtilizedModelResponseList[0].utilizationValue").value(5000));
    }

    @Test
    void ensureGetTopUtilizedModelsWithInvalidCriteriaReturns400() throws Exception {
        when(getTopUtilizedModels.execute("INVALID")).thenThrow(new IllegalArgumentException("Invalid criteria"));

        mockMvc.perform(get("/api/aircraftModels/top-utilized")
                        .param("criteria", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureRegisterModelReturns201() throws Exception {
        RegisterAircraftModelRequest request = new RegisterAircraftModelRequest(
                "A320", Manufacturer.AIRBUS, 6150.0, 26730.0, 833.0, 180, null, null);

        AircraftModelResponse response = new AircraftModelResponse(
                "A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, false, 180);

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
                "", Manufacturer.AIRBUS, 6150.0, 26730.0, 833.0, 180, null, null);

        mockMvc.perform(post("/api/aircraftModels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureRegisterModelWithImageInJsonReturns400() throws Exception {
        String json = "{\"modelName\":\"A320\",\"manufacturer\":\"AIRBUS\",\"maxRange\":6150.0," +
                "\"fuelCapacity\":26730.0,\"cruisingSpeed\":833.0,\"maximumSeatingCapacity\":180," +
                "\"image\":\"AQID\"}";

        mockMvc.perform(post("/api/aircraftModels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureCreateModelWithImageReturns201() throws Exception {
        AircraftModelResponse response = new AircraftModelResponse(
                "A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, true, 180);

        when(registerAircraftModel.execute(any())).thenReturn(response);

        MockMultipartFile image = new MockMultipartFile("image", "a320.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/aircraftModels")
                        .file(image)
                        .param("modelName", "A320")
                        .param("manufacturer", "AIRBUS")
                        .param("maxRange", "6150.0")
                        .param("fuelCapacity", "26730.0")
                        .param("cruisingSpeed", "833.0")
                        .param("maximumSeatingCapacity", "180"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hasImage").value(true));
    }

    @Test
    void ensureCreateModelWithNonImageFileReturns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("image", "doc.txt", "text/plain", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/aircraftModels")
                        .file(file)
                        .param("modelName", "A320")
                        .param("manufacturer", "AIRBUS")
                        .param("maxRange", "6150.0")
                        .param("fuelCapacity", "26730.0")
                        .param("cruisingSpeed", "833.0")
                        .param("maximumSeatingCapacity", "180"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureUpdateImageReturns200() throws Exception {
        AircraftModelResponse response = new AircraftModelResponse(
                "A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, true, 180);

        when(updateAircraftModelImage.execute(any())).thenReturn(response);

        MockMultipartFile image = new MockMultipartFile("image", "a320.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/aircraftModels/A320/image")
                        .file(image)
                        .with(request -> { request.setMethod("PATCH"); return request; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasImage").value(true));
    }

    @Test
    void ensureUpdateImageWithNonImageFileReturns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("image", "doc.txt", "text/plain", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/aircraftModels/A320/image")
                        .file(file)
                        .with(request -> { request.setMethod("PATCH"); return request; }))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureGetImageReturns200() throws Exception {
        when(getAircraftModelImage.execute("A320"))
                .thenReturn(new AircraftModelImageData(new byte[]{1, 2, 3}, "image/jpeg"));

        mockMvc.perform(get("/api/aircraftModels/A320/image"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"));
    }

    @Test
    void ensureGetImageReturns404WhenNoImage() throws Exception {
        when(getAircraftModelImage.execute("A320"))
                .thenThrow(new AircraftModelImageNotFoundException("Aircraft model 'A320' has no image."));

        mockMvc.perform(get("/api/aircraftModels/A320/image"))
                .andExpect(status().isNotFound());
    }

    @Test
    void ensureGetAllModelsReturns200() throws Exception {
        when(listAircraftModels.execute(anyInt(), anyInt())).thenReturn(new PaginatedResult<>(List.of(), 0L));

        mockMvc.perform(get("/api/aircraftModels"))
                .andExpect(status().isOk());
    }

    @Test
    void ensureGetModelDetailsReturns200() throws Exception {
        AircraftModelResponse response = new AircraftModelResponse(
                "A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, false, 180);

        when(viewAircraftModelDetails.execute("A320")).thenReturn(response);

        mockMvc.perform(get("/api/aircraftModels/A320"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelName").value("A320"));
    }

    @Test
    void ensureUpdateModelReturns200() throws Exception {
        AircraftModelResponse response = new AircraftModelResponse(
                "A320", Manufacturer.AIRBUS, 28000.0, 7000.0, 850.0, false, 200);

        when(updateAircraftModel.execute(any(), any())).thenReturn(response);

        mockMvc.perform(patch("/api/aircraftModels/A320")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maxRange\": 28000.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelName").value("A320"));
    }

    @Test
    void ensureDeleteModelReturns204() throws Exception {
        mockMvc.perform(delete("/api/aircraftModels/A320"))
                .andExpect(status().isNoContent());
    }

    @Test
    void ensureImportModelsReturns207WhenPartialSuccess() throws Exception {
        BulkImportResult<AircraftModelResponse> result = new BulkImportResult<>();
        result.addSuccess(new AircraftModelResponse("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, false, 180));
        result.addError(2, "data", "error");

        when(importAircraftModels.execute(any())).thenReturn(result);

        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile("file", "test.csv", "text/csv", "dummy".getBytes());

        mockMvc.perform(multipart("/api/aircraftModels/import").file(file))
                .andExpect(status().isMultiStatus())
                .andExpect(jsonPath("$.successfulCount").value(1))
                .andExpect(jsonPath("$.errorCount").value(1));
    }
}
