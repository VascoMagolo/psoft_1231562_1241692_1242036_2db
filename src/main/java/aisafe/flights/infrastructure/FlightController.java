package aisafe.flights.infrastructure;

import aisafe.flights.application.ImportFlightsUseCase;
import aisafe.flights.application.ScheduleFlightUseCase;
import aisafe.flights.application.ViewScheduledFlightsByAircraftUseCase;
import aisafe.flights.application.dtos.FlightResponse;
import aisafe.flights.application.dtos.ScheduleFlightRequest;
import aisafe.shared.application.dtos.BulkImportResult;
import aisafe.shared.infrastructure.BulkImportResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/flights")
@Tag(name = "Flights", description = "Scheduled Flight Management - WP#3B")
public class FlightController {

    private final ScheduleFlightUseCase scheduleFlight;
    private final ViewScheduledFlightsByAircraftUseCase viewScheduledFlightsByAircraft;
    private final ImportFlightsUseCase importFlightsUseCase;

    public FlightController(ScheduleFlightUseCase scheduleFlight,
                            ViewScheduledFlightsByAircraftUseCase viewScheduledFlightsByAircraft,
                            ImportFlightsUseCase importFlightsUseCase) {
        this.scheduleFlight = scheduleFlight;
        this.viewScheduledFlightsByAircraft = viewScheduledFlightsByAircraft;
        this.importFlightsUseCase = importFlightsUseCase;
    }

    private EntityModel<FlightResponse> toModel(FlightResponse flight) {
        return EntityModel.of(flight,
                linkTo(methodOn(FlightController.class).scheduleFlight(null)).withRel("schedule"),
                linkTo(methodOn(FlightController.class).getScheduledFlightsByAircraft(flight.aircraftId())).withRel("aircraft-flights"));
    }

    @Operation(summary = "Assign aircraft to route", description = "Creates a scheduled flight for an aircraft and route. (US212)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Scheduled flight created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid schedule or business rule violation"),
            @ApiResponse(responseCode = "404", description = "Aircraft or route not found"),
            @ApiResponse(responseCode = "409", description = "Aircraft unavailable in the requested timeframe")
    })
    @PostMapping
    public ResponseEntity<EntityModel<FlightResponse>> scheduleFlight(
            @Valid @RequestBody ScheduleFlightRequest request) {
        FlightResponse response = scheduleFlight.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toModel(response));
    }

    @Operation(summary = "View scheduled flights by aircraft", description = "Lists scheduled flights for a specific aircraft. (US213)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Scheduled flights retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid aircraft identifier"),
            @ApiResponse(responseCode = "404", description = "Aircraft not found")
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<FlightResponse>>> getScheduledFlightsByAircraft(
            @RequestParam String aircraftId) {
        List<EntityModel<FlightResponse>> flights = viewScheduledFlightsByAircraft.execute(aircraftId).stream()
                .map(this::toModel)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(flights,
                linkTo(methodOn(FlightController.class).getScheduledFlightsByAircraft(aircraftId)).withSelfRel()));
    }

    @Operation(summary = "Import flights", description = "Bulk import scheduled flights from a CSV file.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "All flights imported successfully"),
            @ApiResponse(responseCode = "207", description = "Partial success or all failed"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping(value = "/import", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<java.util.Map<String, Object>> importFlights(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        BulkImportResult<String> result = importFlightsUseCase.execute(file);
        return BulkImportResponseBuilder.buildResponse(result);
    }
}
