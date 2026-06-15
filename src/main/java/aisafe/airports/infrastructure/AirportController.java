package aisafe.airports.infrastructure;

import aisafe.airports.application.*;
import aisafe.airports.application.dtos.*;
import aisafe.routes.application.dtos.RouteResponse;
import aisafe.routes.infrastructure.RouteController;
import aisafe.shared.domain.PaginatedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * REST controller for managing airports, including registration, certification,
 * details retrieval, searching, status updates, and statistics.
 */
@RestController
@RequestMapping("/api/airports")
@Tag(name = "Airports", description = "Airport management - WP#2A and WP#2B")
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
    private final DeleteAirportUseCase deleteAirport;

    public AirportController(RegisterAirportUseCase registerAirport,
            AddAirportCertificationUseCase addCertification,
            ViewAirportDetailsUseCase viewAirportDetails,
            SearchAirportUseCase searchAirport,
            UpdateAirportStatusUseCase updateAirportStatus,
            UpdateAirportDetailsUseCase updateAirportDetails,
            ViewAirportRoutesUseCase viewAirportRoutes,
            AirportStatisticsUseCase airportStatistics,
            ListAirportsByRegionUseCase listAirportsByRegion,
            DeleteAirportUseCase deleteAirport) {
        this.registerAirport = registerAirport;
        this.addCertification = addCertification;
        this.viewAirportDetails = viewAirportDetails;
        this.searchAirport = searchAirport;
        this.updateAirportStatus = updateAirportStatus;
        this.updateAirportDetails = updateAirportDetails;
        this.viewAirportRoutes = viewAirportRoutes;
        this.airportStatistics = airportStatistics;
        this.listAirportsByRegion = listAirportsByRegion;
        this.deleteAirport = deleteAirport;
    }

    /**
     * Helper method to convert an AirportResponse DTO into an EntityModel with
     * HATEOAS links for related actions and resources.
     * 
     * @param airport the AirportResponse DTO to convert into an EntityModel
     * @return an EntityModel containing the airport data and links to related
     *         actions and resources
     */
    private EntityModel<AirportResponse> toModel(AirportResponse airport) {
        String code = airport.iataCode();
        return EntityModel.of(airport,
                linkTo(methodOn(AirportController.class).getAirport(code)).withSelfRel(),
                linkTo(methodOn(AirportController.class).updateStatus(code, null)).withRel("update-status"),
                linkTo(methodOn(AirportController.class).updateDetails(code, null)).withRel("update-details"),
                linkTo(methodOn(AirportController.class).getRoutes(code)).withRel("routes"),
                linkTo(methodOn(AirportController.class).addCertification(code, null)).withRel("certifications"));
    }

    /**
     * Registers a new airport with the provided details
     * 
     * @param request the details of the airport to register
     * @return a ResponseEntity containing the details of the newly registered
     *         airport along with appropriate HATEOAS links
     */
    // US106 + US207
    @Operation(summary = "Register a new airport", description = "Creates an airport with runways and optional facilities. Requires Backoffice Operator role. (US106, US207)")
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

    /**
     * Adds an aircraft certification to an airport, indicating that a specific
     * aircraft model is certified to operate at that airport.
     * 
     * @param iataCode the IATA code of the airport to which the certification will
     *                 be added
     * @param request  the details of the aircraft certification to add
     * @return a ResponseEntity containing the details of the added certification
     *         along with a link to the airport it was added to
     */
    // US106a
    @Operation(summary = "Add aircraft certification to airport", description = "Certifies that a specific aircraft model can operate at this airport. Requires Backoffice Operator or ATCC role. (US106a)")
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

    /**
     * Retrieves the details of a specific airport based on its IATA code
     * 
     * @param iataCode the 3-letter IATA code of the airport to retrieve
     * @return a ResponseEntity containing the details of the specified airport
     *         along with appropriate HATEOAS links to related actions and resources
     */
    // US107
    @Operation(summary = "Get airport details by IATA code", description = "Returns full airport details including runways, contacts, and facilities. Requires Backoffice Operator or ATCC role. (US107)")
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

    /**
     * Searches for airports based on optional filters
     * 
     * @param name     (optional) Filter by airport name
     * @param city     (optional) Filter by city
     * @param country  (optional) Filter by country
     * @param pageable Pagination information for the search results
     * @return a ResponseEntity containing a page of search results matching the
     *         provided criteria
     */
    // US108
    @Operation(summary = "Search airports", description = "Search airports by name, city, and/or country. All parameters are optional. Requires ATCC role. (US108)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results returned (may be empty)"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @GetMapping("/search")
    public ResponseEntity<PagedModel<EntityModel<AirportResponse>>> searchAirports(
            @Parameter(description = "Filter by airport name (partial match)") @RequestParam(required = false) String name,
            @Parameter(description = "Filter by city") @RequestParam(required = false) String city,
            @Parameter(description = "Filter by country") @RequestParam(required = false) String country,
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<AirportResponse> assembler) {
        PaginatedResult<AirportResponse> result = searchAirport.execute(name, city, country, pageable.getPageNumber(), pageable.getPageSize());
        Page<AirportResponse> page = new PageImpl<>(result.data(), pageable, result.totalElements());
        return ResponseEntity.ok(assembler.toModel(page, this::toModel));
    }

    /**
     * Updates the operational status of an airport, allowing it to be set to
     * OPERATIONAL, CLOSED, or UNDER_MAINTENANCE.
     * 
     * @param iataCode the 3-letter IATA code of the airport to update
     * @param request  the new status to set for the airport
     * @return a ResponseEntity containing the updated details of the airport after
     *         the status change, along with appropriate HATEOAS links to related
     *         actions and resources
     */
    // US109
    @Operation(summary = "Update airport operational status", description = "Changes the airport status to OPERATIONAL, CLOSED, or UNDER_MAINTENANCE. Requires Backoffice Operator role. (US109)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing status value"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Airport not found"),
            @ApiResponse(responseCode = "409", description = "Concurrent update conflict -- please retry")
    })
    @PatchMapping("/{iataCode}/status")
    public ResponseEntity<EntityModel<AirportResponse>> updateStatus(
            @Parameter(description = "3-letter IATA airport code", example = "LIS") @PathVariable String iataCode,
            @Valid @RequestBody UpdateAirportStatusRequest request) {
        return ResponseEntity.ok(toModel(updateAirportStatus.execute(iataCode.toUpperCase(), request.status())));
    }

    /**
     * Updates the details of an existing airport
     * 
     * @param iataCode the 3-letter IATA code of the airport to update
     * @param request  the new details to update for the airport
     * @return a ResponseEntity containing the updated details of the airport after
     *         the update is applied, along with appropriate HATEOAS links to
     *         related actions and resources
     */
    // US208
    @Operation(summary = "Update airport details", description = "Updates optional fields: operational hours, contact information, image, services, terminals, and gates. Requires Backoffice Operator role. (US208)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Details updated successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Airport not found"),
            @ApiResponse(responseCode = "409", description = "Concurrent update conflict -- please retry")
    })
    @PatchMapping("/{iataCode}/details")
    public ResponseEntity<EntityModel<AirportResponse>> updateDetails(
            @Parameter(description = "3-letter IATA airport code", example = "LIS") @PathVariable String iataCode,
            @Valid @RequestBody UpdateAirportDetailsRequest request) {
        return ResponseEntity.ok(toModel(updateAirportDetails.execute(iataCode.toUpperCase(), request)));
    }

    /**
     * Retrieves all routes that depart from or arrive at the specified airport,
     * identified by its IATA code.
     * 
     * @param iataCode the 3-letter IATA code of the airport for which to retrieve
     *                 associated routes
     * @return a ResponseEntity containing a list of Route objects representing all
     *         routes that depart from or arrive at the specified airport. The list
     *         may be empty if there are no associated routes.
     */
    // US209
    @Operation(summary = "Get routes for an airport", description = "Returns all routes that depart from or arrive at the given airport. Requires ATCC role. (US209)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Routes returned (may be empty)"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Airport not found")
    })
    @GetMapping("/{iataCode}/routes")
    public ResponseEntity<CollectionModel<EntityModel<RouteResponse>>> getRoutes(
            @Parameter(description = "3-letter IATA airport code", example = "LIS") @PathVariable String iataCode) {
        List<EntityModel<RouteResponse>> routeModels = viewAirportRoutes.execute(iataCode.toUpperCase())
                .stream()
                .map(r -> EntityModel.of(r,
                        linkTo(methodOn(RouteController.class).getRouteDetails(r.originIataCode(), r.destinationIataCode())).withSelfRel()))
                .toList();
        return ResponseEntity.ok(CollectionModel.of(routeModels,
                linkTo(methodOn(AirportController.class).getRoutes(iataCode)).withSelfRel(),
                linkTo(methodOn(AirportController.class).getAirport(iataCode)).withRel("airport")));
    }

    /**
     * Retrieves statistics about the busiest airports based on the total number of
     * associated routes.
     * 
     * @return a ResponseEntity containing a list of AirportStatisticsResponse
     *         objects representing the busiest airports ranked by total number of
     *         associated routes.
     */
    // US210
    @Operation(summary = "Get busiest airports by number of routes", description = "Returns airports ranked by total number of associated routes. Requires Backoffice Operator role. (US210)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @GetMapping("/statistics/busiest")
    public ResponseEntity<CollectionModel<EntityModel<AirportStatisticsResponse>>> getBusiestAirports() {
        List<EntityModel<AirportStatisticsResponse>> items = airportStatistics.execute()
                .stream()
                .map(s -> EntityModel.of(s,
                        linkTo(methodOn(AirportController.class).getAirport(s.iataCode())).withRel("airport")))
                .toList();
        return ResponseEntity.ok(CollectionModel.of(items,
                linkTo(methodOn(AirportController.class).getBusiestAirports()).withSelfRel()));
    }

    /**
     * Retrieves a list of airports grouped by either region or country, based on
     * the specified grouping criterion.
     * 
     * @param by the grouping criterion, which can be either "region" (default) or
     *           "country".
     * @return a ResponseEntity containing a list of AirportGroupResponse objects
     *         representing airports grouped by the specified criterion.
     */
    // US211
    @Operation(summary = "List airports grouped by region or country", description = "Returns airports grouped by region (default) or country. Use ?by=country to group by country. Requires ATCC role. (US211)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Grouped airports returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @GetMapping("/grouped")
    public ResponseEntity<CollectionModel<AirportGroupResponse>> getAirportsGrouped(
            @Parameter(description = "Grouping criterion: 'region' (default) or 'country'") @RequestParam(defaultValue = "region") String by) {
        return ResponseEntity.ok(CollectionModel.of(
                listAirportsByRegion.execute(by),
                linkTo(methodOn(AirportController.class).getAirportsGrouped(by)).withSelfRel()));
    }

    @Operation(summary = "Delete an airport", description = "Permanently removes an airport by IATA code. Requires Admin role.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Airport deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Airport not found")
    })
    @DeleteMapping("/{iataCode}")
    public ResponseEntity<Void> deleteAirport(
            @Parameter(description = "3-letter IATA airport code", example = "LIS") @PathVariable String iataCode) {
        deleteAirport.execute(iataCode.toUpperCase());
        return ResponseEntity.noContent().build();
    }
}
