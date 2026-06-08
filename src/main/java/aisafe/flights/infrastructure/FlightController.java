package aisafe.flights.infrastructure;

import aisafe.flights.application.ScheduleFlightUseCase;
import aisafe.flights.application.ViewScheduledFlightsByAircraftUseCase;
import aisafe.flights.application.dtos.FlightResponse;
import aisafe.flights.application.dtos.ScheduleFlightRequest;
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

    public FlightController(ScheduleFlightUseCase scheduleFlight,
                            ViewScheduledFlightsByAircraftUseCase viewScheduledFlightsByAircraft) {
        this.scheduleFlight = scheduleFlight;
        this.viewScheduledFlightsByAircraft = viewScheduledFlightsByAircraft;
    }

    private EntityModel<FlightResponse> toModel(FlightResponse flight) {
        return EntityModel.of(flight,
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
        FlightResponse response = FlightResponse.from(scheduleFlight.execute(request));
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
                .map(FlightResponse::from)
                .map(this::toModel)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(flights,
                linkTo(methodOn(FlightController.class).getScheduledFlightsByAircraft(aircraftId)).withSelfRel()));
    }
}
