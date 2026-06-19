package aisafe.airports.infrastructure;

import aisafe.airports.application.*;
import aisafe.airports.application.dtos.*;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportPhotoNotFoundException;
import aisafe.airports.domain.AirportStatus;
import aisafe.routes.application.dtos.RouteResponse;
import aisafe.routes.domain.RouteStatus;
import aisafe.security.application.JwtService;
import aisafe.security.domain.UserRepository;
import aisafe.shared.domain.ConcurrencyException;
import aisafe.shared.domain.PaginatedResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AirportController.class)
@AutoConfigureMockMvc(addFilters = false)
class AirportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private RegisterAirportUseCase registerAirport;

    @MockitoBean
    private AddAirportCertificationUseCase addCertification;

    @MockitoBean
    private ViewAirportDetailsUseCase viewAirportDetails;

    @MockitoBean
    private SearchAirportUseCase searchAirport;

    @MockitoBean
    private UpdateAirportStatusUseCase updateAirportStatus;

    @MockitoBean
    private UpdateAirportDetailsUseCase updateAirportDetails;

    @MockitoBean
    private ViewAirportRoutesUseCase viewAirportRoutes;

    @MockitoBean
    private AirportStatisticsUseCase airportStatistics;

    @MockitoBean
    private ListAirportsByRegionUseCase listAirportsByRegion;

    @MockitoBean
    private DeleteAirportUseCase deleteAirport;

    private UploadAirportPhotoUseCase uploadAirportPhoto;

    @MockitoBean
    private GetAirportPhotoUseCase getAirportPhoto;

    @MockitoBean
    private ImportAirportsUseCase importAirports;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private AirportResponse sampleAirportResponse;

    @BeforeEach
    void setUp() {
        sampleAirportResponse = new AirportResponse(
                "LIS", "Lisbon Airport", "Lisbon", "Portugal", "Europe",
                "Europe/Lisbon", 0, null, "OPERATIONAL",
                new AirportResponse.CoordinatesRecord(38.77, -9.13),
                List.of(new AirportResponse.RunwayRecord("03/21", 3000, "030/210")),
                List.of(), List.of(), List.of(), List.of(), 0L);
    }

    @Test
    void ensureRegisterAirportReturns201() throws Exception {
        RegisterAirportRequest request = new RegisterAirportRequest(
                "LIS", "Lisbon Airport", "Lisbon", "Portugal", "Europe", "Europe/Lisbon",
                38.77, -9.13,
                List.of(new RegisterAirportRequest.RunwayRequest("03/21", 3000, "030/210")),
                null, null, null, null, null, null);

        when(registerAirport.execute(any())).thenReturn(sampleAirportResponse);

        mockMvc.perform(post("/api/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.iataCode").value("LIS"))
                .andExpect(jsonPath("$._links").exists());
    }

    @Test
    void ensureRegisterAirportWithMissingRunwayReturns400() throws Exception {
        RegisterAirportRequest request = new RegisterAirportRequest(
                "LIS", "Lisbon Airport", "Lisbon", "Portugal", "Europe", "Europe/Lisbon",
                38.77, -9.13,
                List.of(),
                null, null, null, null, null, null);

        mockMvc.perform(post("/api/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureRegisterAirportWithImageFieldInJsonReturns400() throws Exception {
        RegisterAirportRequest request = new RegisterAirportRequest(
                "LIS", "Lisbon Airport", "Lisbon", "Portugal", "Europe", "Europe/Lisbon",
                38.77, -9.13,
                List.of(new RegisterAirportRequest.RunwayRequest("03/21", 3000, "030/210")),
                new byte[]{1, 2, 3}, "image/jpeg", null, null, null, null);

        mockMvc.perform(post("/api/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureAddPhotoReturns200() throws Exception {
        when(uploadAirportPhoto.execute(anyString(), any(), anyString())).thenReturn(sampleAirportResponse);

        MockMultipartFile photo = new MockMultipartFile("photo", "lis.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/airports/LIS/photos")
                        .file(photo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iataCode").value("LIS"));
    }

    @Test
    void ensureAddPhotoWithNonImageFileReturns400() throws Exception {
        MockMultipartFile photo = new MockMultipartFile("photo", "file.txt", "text/plain", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/airports/LIS/photos")
                        .file(photo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureGetPhotoByIndexReturns200() throws Exception {
        when(getAirportPhoto.execute(anyString(), anyInt()))
                .thenReturn(new AirportPhotoData(new byte[]{1, 2, 3}, "image/jpeg"));

        mockMvc.perform(get("/api/airports/LIS/photos/0"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"));
    }

    @Test
    void ensureGetPhotoReturns404WhenNoPhotoAtIndex() throws Exception {
        when(getAirportPhoto.execute(anyString(), anyInt()))
                .thenThrow(new AirportPhotoNotFoundException("LIS"));

        mockMvc.perform(get("/api/airports/LIS/photos/0"))
                .andExpect(status().isNotFound());
    }

    @Test
    void ensureGetPhotoReturns404WhenAirportNotFound() throws Exception {
        when(getAirportPhoto.execute(anyString(), anyInt()))
                .thenThrow(new AirportNotFoundException("LIS"));

        mockMvc.perform(get("/api/airports/LIS/photos/0"))
                .andExpect(status().isNotFound());
    }

    @Test
    void ensureGetAirportByIataCodeReturns200() throws Exception {
        when(viewAirportDetails.execute(anyString())).thenReturn(sampleAirportResponse);

        mockMvc.perform(get("/api/airports/LIS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iataCode").value("LIS"))
                .andExpect(jsonPath("$.status").value("OPERATIONAL"));
    }

    @Test
    void ensureUpdateAirportStatusReturns200() throws Exception {
        UpdateAirportStatusRequest request = new UpdateAirportStatusRequest(AirportStatus.CLOSED);

        when(updateAirportStatus.execute(anyString(), any(), anyLong())).thenReturn(sampleAirportResponse);

        mockMvc.perform(patch("/api/airports/LIS/status")
                        .header(HttpHeaders.IF_MATCH, "\"0\"")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iataCode").value("LIS"));
    }

    @Test
    void ensureUpdateAirportStatusReturns400WhenIfMatchMissing() throws Exception {
        UpdateAirportStatusRequest request = new UpdateAirportStatusRequest(AirportStatus.CLOSED);

        mockMvc.perform(patch("/api/airports/LIS/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureUpdateAirportStatusReturns412OnVersionMismatch() throws Exception {
        UpdateAirportStatusRequest request = new UpdateAirportStatusRequest(AirportStatus.CLOSED);

        when(updateAirportStatus.execute(anyString(), any(), anyLong()))
                .thenThrow(new ConcurrencyException("Airport version mismatch. Please fetch the latest version and retry."));

        mockMvc.perform(patch("/api/airports/LIS/status")
                        .header(HttpHeaders.IF_MATCH, "\"0\"")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isPreconditionFailed());
    }

    @Test
    void ensureUpdateAirportDetailsReturns200() throws Exception {
        UpdateAirportDetailsRequest request = new UpdateAirportDetailsRequest(
                "06:00-23:00", null, null, null, null);

        when(updateAirportDetails.execute(anyString(), any(), anyLong())).thenReturn(sampleAirportResponse);

        mockMvc.perform(patch("/api/airports/LIS/details")
                        .header(HttpHeaders.IF_MATCH, "\"0\"")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iataCode").value("LIS"));
    }

    @Test
    void ensureUpdateAirportDetailsReturns400WhenIfMatchMissing() throws Exception {
        UpdateAirportDetailsRequest request = new UpdateAirportDetailsRequest(
                "06:00-23:00", null, null, null, null);

        mockMvc.perform(patch("/api/airports/LIS/details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureUpdateAirportDetailsReturns412OnVersionMismatch() throws Exception {
        UpdateAirportDetailsRequest request = new UpdateAirportDetailsRequest(
                "06:00-23:00", null, null, null, null);

        when(updateAirportDetails.execute(anyString(), any(), anyLong()))
                .thenThrow(new ConcurrencyException("Airport version mismatch. Please fetch the latest version and retry."));

        mockMvc.perform(patch("/api/airports/LIS/details")
                        .header(HttpHeaders.IF_MATCH, "\"0\"")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isPreconditionFailed());
    }

    @Test
    void ensureAddCertificationReturns201() throws Exception {
        AddCertificationRequest request = new AddCertificationRequest("A320");

        when(addCertification.execute(anyString(), any())).thenReturn(new AircraftCertificationResponse("LIS", "A320"));

        mockMvc.perform(post("/api/airports/LIS/certifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.airportIataCode").value("LIS"))
                .andExpect(jsonPath("$.aircraftModelName").value("A320"));
    }

    @Test
    void ensureSearchAirportsReturns200() throws Exception {
        when(searchAirport.execute(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PaginatedResult<>(List.of(sampleAirportResponse), 1));

        mockMvc.perform(get("/api/airports/search")
                        .param("name", "Lisbon"))
                .andExpect(status().isOk());
    }

    @Test
    void ensureGetRoutesReturns200() throws Exception {
        when(viewAirportRoutes.execute(anyString()))
                .thenReturn(List.of(new RouteResponse("LIS", "OPO", 60, 500.0, 100, RouteStatus.ACTIVE, 0L)));

        mockMvc.perform(get("/api/airports/LIS/routes"))
                .andExpect(status().isOk());
    }

    @Test
    void ensureGetBusiestAirportsReturns200() throws Exception {
        when(airportStatistics.execute())
                .thenReturn(List.of(new AirportStatisticsResponse("LIS", "Lisbon Airport", "Lisbon", "Portugal", 5)));

        mockMvc.perform(get("/api/airports/statistics/busiest"))
                .andExpect(status().isOk());
    }

    @Test
    void ensureGetAirportsGroupedReturns200() throws Exception {
        when(listAirportsByRegion.execute(any()))
                .thenReturn(List.of(new AirportGroupResponse("Europe", List.of())));

        mockMvc.perform(get("/api/airports/grouped"))
                .andExpect(status().isOk());
    }

    @Test
    void ensureDeleteAirportReturns204() throws Exception {
        mockMvc.perform(delete("/api/airports/OPO"))
                .andExpect(status().isNoContent());
    }

    @Test
    void ensureImportAirportsReturns201OnFullSuccess() throws Exception {
        aisafe.shared.application.dtos.BulkImportResult<String> result = new aisafe.shared.application.dtos.BulkImportResult<>();
        result.addSuccess("OPO");
        result.addSuccess("LIS");

        when(importAirports.execute(any())).thenReturn(result);

        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile(
                "file", "airports.csv", "text/csv", "iataCode,name,city,country\nOPO,Porto,Porto,Portugal\nLIS,Lisbon,Lisbon,Portugal".getBytes());

        mockMvc.perform(multipart("/api/airports/import").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.successfulCount").value(2))
                .andExpect(jsonPath("$.errorCount").value(0));
    }
}
