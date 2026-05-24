package aisafe.routes.infrastructure;

import aisafe.routes.application.*;
import aisafe.routes.application.dtos.CreateRouteRequest;
import aisafe.routes.application.dtos.RouteHistoryResponse;
import aisafe.routes.application.dtos.RouteResponse;
import aisafe.routes.application.dtos.UpdateRouteRequest;
import aisafe.routes.domain.Route;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/routes")
@Tag(name = "Routes", description = "Flight Routes Management - WP#3")
public class RouteController {

    private final CreateRouteUseCase createRoute;
    private final ViewRouteHistoryUseCase viewRouteHistory;
    private final UpdateRouteUseCase updateRoute;
    private final DesactivateRouteUseCase desactivateRoute;
    private final ViewRouteDetailsUseCase viewRouteDetails;
    private final ListRoutesFromAirportUseCase listRoutesFromAirport;
    private final SearchRoutesUseCase searchRoutes;

    /**
     * Instantiates a new Route controller.
     *
     * @param createRoute           the create route
     * @param viewRouteHistory      the view route history
     * @param updateRoute           the update route
     * @param desactivateRoute      the desactivate route
     * @param viewRouteDetails      the view route details
     * @param listRoutesFromAirport the list routes from airport
     * @param searchRoutes          the search routes
     */
    public RouteController(CreateRouteUseCase createRoute,
                           ViewRouteHistoryUseCase viewRouteHistory,
                           UpdateRouteUseCase updateRoute,
                           DesactivateRouteUseCase desactivateRoute,
                           ViewRouteDetailsUseCase viewRouteDetails,
                           ListRoutesFromAirportUseCase listRoutesFromAirport,
                           SearchRoutesUseCase searchRoutes) {
        this.createRoute = createRoute;
        this.viewRouteHistory = viewRouteHistory;
        this.updateRoute = updateRoute;
        this.desactivateRoute = desactivateRoute;
        this.viewRouteDetails = viewRouteDetails;
        this.listRoutesFromAirport = listRoutesFromAirport;
        this.searchRoutes = searchRoutes;
    }

    private EntityModel<RouteResponse> toModel(RouteResponse route) {
        Long id = route.id();
        return EntityModel.of(route,
                linkTo(methodOn(RouteController.class).getRouteDetails(id)).withSelfRel(),
                linkTo(methodOn(RouteController.class).getRouteHistory(id)).withRel("history"),
                linkTo(methodOn(RouteController.class).updateRoute(id, null, null)).withRel("update"),
                linkTo(methodOn(RouteController.class).deactivateRoute(id, null)).withRel("deactivate"));
    }

    private EntityModel<RouteResponse> mapToModel(Route r) {
        RouteResponse response = new RouteResponse(
                r.getId(),
                r.getOrigin().getCode(),
                r.getDestination().getCode(),
                r.getEstimatedFlightTime(),
                r.getMinimumRange(),
                r.getMinimumCapacity(),
                r.isActive());
        return toModel(response);
    }

    /**
     * Creates a new flight route in the system.
     *
     * @param request the request containing the route information
     * @return a response entity containing the created route
     */
    // US110
    @Operation(summary = "Create a flight route", description = "Registers a new flight route in the system. (US110)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Route created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data supplied"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PostMapping
    public ResponseEntity<EntityModel<RouteResponse>> createRoute(
            @Valid @RequestBody CreateRouteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToModel(createRoute.execute(request)));
    }

    /**
     * Retrieves the complete history of changes made to a route.
     *
     * @param id the unique identifier of the route
     * @return a response entity containing the route history entries
     */
    // US111
    @Operation(summary = "Keep track of route history", description = "Retrieves the historical changes and updates made to a specific route. (US111)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Route history retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Route not found")
    })
    @GetMapping("/{id}/history")
    public ResponseEntity<CollectionModel<EntityModel<RouteHistoryResponse>>> getRouteHistory(
            @Parameter(description = "Unique ID of the route") @PathVariable Long id) {
        List<EntityModel<RouteHistoryResponse>> historyModels = viewRouteHistory.execute(id).stream()
                .map(h -> {
                    RouteHistoryResponse response = new RouteHistoryResponse(
                            h.getId(), h.getRoute().getId(), h.getChangeDescription(),
                            h.getChangedAt(), h.getChangedBy());
                    return EntityModel.of(response,
                            linkTo(methodOn(RouteController.class).getRouteDetails(response.routeId()))
                                    .withRel("route"));
                })
                .toList();
        return ResponseEntity.ok(CollectionModel.of(historyModels,
                linkTo(methodOn(RouteController.class).getRouteHistory(id)).withSelfRel(),
                linkTo(methodOn(RouteController.class).getRouteDetails(id)).withRel("route")));
    }

    /**
     * Updates the information of an existing route.
     *
     * @param id the unique identifier of the route to update
        * @param request the request containing the updated route data
     * @return a response entity containing the updated route
     */
    // US112
    @Operation(summary = "Update route details", description = "Updates the information of an existing flight route. (US112)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Route updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data supplied"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Route not found"),
            @ApiResponse(responseCode = "412", description = "ETag validation failed")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<RouteResponse>> updateRoute(
            @Parameter(description = "Unique ID of the route to be updated") @PathVariable Long id,
            @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch,
            @Valid @RequestBody UpdateRouteRequest request) {
        return ResponseEntity.ok(mapToModel(updateRoute.execute(id, request)));
    }

    /**
     * Deactivates an existing route.
     *
     * @param id the unique identifier of the route to deactivate
     * @return a response entity containing the deactivated route
     */
    // US112
    @Operation(summary = "Deactivate a route", description = "Sets an active flight route status to deactivated. (US112)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Route deactivated successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Route not found"),
            @ApiResponse(responseCode = "412", description = "ETag validation failed")
    })
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<EntityModel<RouteResponse>> deactivateRoute(
            @Parameter(description = "Unique ID of the route to deactivate") @PathVariable Long id,
            @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        return ResponseEntity.ok(mapToModel(desactivateRoute.execute(id)));
    }

    /**
     * Retrieves all active routes departing from a specific airport.
     *
     * @param iataCode the IATA code of the origin airport
     * @param pageable pagination parameters
     * @param assembler the paged resources assembler
     * @return a response entity containing the paginated routes
     */
    // US113
    @Operation(summary = "View route details", description = "Retrieves the full details of a specific route by its ID. (US113)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Route details retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Route not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<RouteResponse>> getRouteDetails(
            @Parameter(description = "Unique ID of the route") @PathVariable Long id) {
        return ResponseEntity.ok(mapToModel(viewRouteDetails.execute(id)));
    }

    /**
     * Retrieves all active routes departing from a specific airport.
     *
     * @param iataCode the IATA code of the origin airport
     * @return a response entity containing the list of routes
     */
    // US113
    @Operation(summary = "View routes from airport", description = "Retrieves all active routes originating from a specific airport. (US113)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Routes retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Airport not found")
    })
    @GetMapping("/airport/{iataCode}")
    public ResponseEntity<PagedModel<EntityModel<RouteResponse>>> getRoutesFromAirport(
            @Parameter(description = "IATA code of the origin airport (e.g., LIS, OPO)") @PathVariable String iataCode,
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<Route> assembler) {
        Page<Route> routePage = listRoutesFromAirport.execute(iataCode.toUpperCase(), pageable);
        return ResponseEntity.ok(assembler.toModel(routePage, this::mapToModel));
    }

    /**
     * Searches for routes based on origin and destination filters.
     *
     * @param origin the origin airport or location filter
     * @param destination the destination airport or location filter
     * @param pageable pagination parameters
     * @param assembler the paged resources assembler
     * @return a response entity containing the matching paginated routes
     */
    // US114
    @Operation(summary = "Search routes", description = "Searches for flight routes based on origin and/or destination criteria. (US114)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @GetMapping("/search")
    public ResponseEntity<PagedModel<EntityModel<RouteResponse>>> searchRoutes(
            @Parameter(description = "Origin location or IATA code") @RequestParam(required = false) String origin,
            @Parameter(description = "Destination location or IATA code") @RequestParam(required = false) String destination,
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<Route> assembler) {
        Page<Route> routePage = this.searchRoutes.execute(origin, destination, pageable);
        return ResponseEntity.ok(assembler.toModel(routePage, this::mapToModel));
    }
}