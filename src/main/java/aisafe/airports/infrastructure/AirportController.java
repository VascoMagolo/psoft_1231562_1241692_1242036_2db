package aisafe.airports.infrastructure;

import aisafe.airports.application.*;
import aisafe.airports.application.dtos.*;
import aisafe.model.entities.Route;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/airports")
@Tag(name = "Airports", description = "Airport management — WP#2A and WP#2B")
public class AirportController {
    private final RegisterAirportUseCase registerAirport;
    private final AddAirportCertificationUseCase addCertification;
    private final ViewAirportDetailsUseCase viewAirportDetails;
    private final SearchAirportUseCase searchAirport;
    private final UpdateAirportStatusUseCase updateAirportStatus;
    private final UpdateAirportDetailsUseCase updateAirportDetails;
    private final ViewAirportRoutesUseCase viewAirportRoutes;
    private final AirportStatisticsUseCase airportStatistics;
    private final ListAirportsByRegionUseCase listAirportsByRegion;

    public AirportController(RegisterAirportUseCase registerAirport,
            AddAirportCertificationUseCase addCertification,
            ViewAirportDetailsUseCase viewAirportDetails,
            SearchAirportUseCase searchAirport,
            UpdateAirportStatusUseCase updateAirportStatus,
            UpdateAirportDetailsUseCase updateAirportDetails,
            ViewAirportRoutesUseCase viewAirportRoutes,
            AirportStatisticsUseCase airportStatistics,
            ListAirportsByRegionUseCase listAirportsByRegion) {
        this.registerAirport = registerAirport;
        this.addCertification = addCertification;
        this.viewAirportDetails = viewAirportDetails;
        this.searchAirport = searchAirport;
        this.updateAirportStatus = updateAirportStatus;
        this.updateAirportDetails = updateAirportDetails;
        this.viewAirportRoutes = viewAirportRoutes;
        this.airportStatistics = airportStatistics;
        this.listAirportsByRegion = listAirportsByRegion;
    }

    private EntityModel<AirportResponse> toModel(AirportResponse airport) {
        String code = airport.iataCode();
        return EntityModel.of(airport,
                linkTo(methodOn(AirportController.class).getAirport(code)).withSelfRel(),
                linkTo(methodOn(AirportController.class).updateStatus(code, null)).withRel("update-status"),
                linkTo(methodOn(AirportController.class).updateDetails(code, null)).withRel("update-details"),
                linkTo(methodOn(AirportController.class).getRoutes(code)).withRel("routes"),
                linkTo(methodOn(AirportController.class).addCertification(code, null)).withRel("certifications"));
    }

    // US106 + US207
    @Operation(summary = "Register a new airport", description = "Creates an airport with runways and optional facilities. Requires Backoffice Operator role.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Airport registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data (e.g. bad IATA code, missing required fields)"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "409", description = "Airport with this IATA code already exists")
    })
    @PostMapping
    public ResponseEntity<EntityModel<AirportResponse>> registerAirport(
            @Valid @RequestBody RegisterAirportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toModel(registerAirport.execute(request)));
    }

    // US106a
    @Operation(summary = "Add aircraft certification to airport", description = "Certifies that a specific aircraft model can operate at this airport. Requires Backoffice Operator or ATCC role.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Certification added successfully"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid aircraft model ID"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Airport or aircraft model not found"),
            @ApiResponse(responseCode = "409", description = "Aircraft model is already certified for this airport")
    })
    @PostMapping("/{iataCode}/certifications")
    public ResponseEntity<EntityModel<AircraftCertificationResponse>> addCertification(
            @Parameter(description = "3-letter IATA airport code", example = "LIS") @PathVariable String iataCode,
            @Valid @RequestBody AddCertificationRequest request) {
        AircraftCertificationResponse cert = addCertification.execute(iataCode.toUpperCase(), request);
        EntityModel<AircraftCertificationResponse> model = EntityModel.of(cert,
                linkTo(methodOn(AirportController.class).getAirport(iataCode)).withRel("airport"));
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    // US107
    @Operation(summary = "Get airport details by IATA code", description = "Returns full airport details including runways, contacts, and facilities. Requires Backoffice Operator or ATCC role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Airport found"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Airport not found")
    })
    @GetMapping("/{iataCode}")
    public ResponseEntity<EntityModel<AirportResponse>> getAirport(
            @Parameter(description = "3-letter IATA airport code", example = "LIS") @PathVariable String iataCode) {
        return ResponseEntity.ok(toModel(viewAirportDetails.execute(iataCode.toUpperCase())));
    }

    // US108
    @Operation(summary = "Search airports", description = "Search airports by name, city, and/or country. All parameters are optional. Requires ATCC role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results returned (may be empty)"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<EntityModel<AirportResponse>>> searchAirports(
            @Parameter(description = "Filter by airport name (partial match)") @RequestParam(required = false) String name,
            @Parameter(description = "Filter by city") @RequestParam(required = false) String city,
            @Parameter(description = "Filter by country") @RequestParam(required = false) String country,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(searchAirport.execute(name, city, country, pageable).map(this::toModel));
    }

    // US109
    @Operation(summary = "Update airport operational status", description = "Changes the airport status to OPERATIONAL, CLOSED, or UNDER_MAINTENANCE. Requires Backoffice Operator role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing status value"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Airport not found"),
            @ApiResponse(responseCode = "409", description = "Concurrent update conflict — please retry")
    })
    @PatchMapping("/{iataCode}/status")
    public ResponseEntity<EntityModel<AirportResponse>> updateStatus(
            @Parameter(description = "3-letter IATA airport code", example = "LIS") @PathVariable String iataCode,
            @Valid @RequestBody UpdateAirportStatusRequest request) {
        return ResponseEntity.ok(toModel(updateAirportStatus.execute(iataCode.toUpperCase(), request.status())));
    }

    // US208
    @Operation(summary = "Update airport details", description = "Updates optional fields: operational hours, contact information, image, services, terminals, and gates. Requires Backoffice Operator role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Details updated successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Airport not found"),
            @ApiResponse(responseCode = "409", description = "Concurrent update conflict — please retry")
    })
    @PatchMapping("/{iataCode}/details")
    public ResponseEntity<EntityModel<AirportResponse>> updateDetails(
            @Parameter(description = "3-letter IATA airport code", example = "LIS") @PathVariable String iataCode,
            @RequestBody UpdateAirportDetailsRequest request) {
        return ResponseEntity.ok(toModel(updateAirportDetails.execute(iataCode.toUpperCase(), request)));
    }

    // US209
    @Operation(summary = "Get routes for an airport", description = "Returns all routes that depart from or arrive at the given airport. Requires ATCC role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Routes returned (may be empty)"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Airport not found")
    })
    @GetMapping("/{iataCode}/routes")
    public ResponseEntity<List<Route>> getRoutes(
            @Parameter(description = "3-letter IATA airport code", example = "LIS") @PathVariable String iataCode) {
        return ResponseEntity.ok(viewAirportRoutes.execute(iataCode.toUpperCase()));
    }

    // US210
    @Operation(summary = "Get busiest airports by number of routes", description = "Returns airports ranked by total number of associated routes. Requires Backoffice Operator role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @GetMapping("/statistics/busiest")
    public ResponseEntity<List<AirportStatisticsResponse>> getBusiestAirports() {
        return ResponseEntity.ok(airportStatistics.execute());
    }

    // US211
    @Operation(summary = "List airports grouped by region or country", description = "Returns airports grouped by region (default) or country. Use ?by=country to group by country. Requires ATCC role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Grouped airports returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @GetMapping("/grouped")
    public ResponseEntity<List<AirportGroupResponse>> getAirportsGrouped(
            @Parameter(description = "Grouping criterion: 'region' (default) or 'country'") @RequestParam(defaultValue = "region") String by) {
        return ResponseEntity.ok(listAirportsByRegion.execute(by));
    }
}
