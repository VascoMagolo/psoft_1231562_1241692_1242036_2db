package aisafe.routes.infrastructure;

import aisafe.routes.application.*;
import aisafe.routes.application.dtos.CreateRouteRequest;
import aisafe.routes.application.dtos.UpdateRouteRequest;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteHistory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller responsible for managing flight routes.
 * Provides endpoints for creating, updating, searching,
 * viewing and deactivating routes.
 */
@RestController
@RequestMapping("/api/routes")
@Tag(name = "Routes", description = "Flight Routes Management")
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
    public ResponseEntity<Route> createRoute(@Valid @RequestBody CreateRouteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createRoute.execute(request));
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
    public ResponseEntity<List<RouteHistory>> getRouteHistory(
            @Parameter(description = "Unique ID of the route") @PathVariable Long id) {
        return ResponseEntity.ok(viewRouteHistory.execute(id));
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
            @ApiResponse(responseCode = "404", description = "Route not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Route> updateRoute(
            @Parameter(description = "Unique ID of the route to be updated") @PathVariable Long id,
            @Valid @RequestBody UpdateRouteRequest request) {
        return ResponseEntity.ok(updateRoute.execute(id, request));
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
            @ApiResponse(responseCode = "404", description = "Route not found")
    })
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Route> deactivateRoute(
            @Parameter(description = "Unique ID of the route to deactivate") @PathVariable Long id) {
        return ResponseEntity.ok(desactivateRoute.execute(id));
    }

    /**
     * Retrieves detailed information about a specific route.
     *
     * @param id the unique identifier of the route
     * @return a response entity containing the route details
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
    public ResponseEntity<Route> getRouteDetails(
            @Parameter(description = "Unique ID of the route") @PathVariable Long id) {
        return ResponseEntity.ok(viewRouteDetails.execute(id));
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
    public ResponseEntity<List<Route>> getRoutesFromAirport(
            @Parameter(description = "IATA code of the origin airport (e.g., LIS, OPO)") @PathVariable String iataCode) {
        return ResponseEntity.ok(listRoutesFromAirport.execute(iataCode.toUpperCase()));
    }

    /**
     * Searches for routes based on origin and destination filters.
     *
     * @param origin the origin airport or location filter
     * @param destination the destination airport or location filter
     * @return a response entity containing the matching routes
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
    public ResponseEntity<List<Route>> searchRoutes(
            @Parameter(description = "Origin location or IATA code") @RequestParam(required = false) String origin,
            @Parameter(description = "Destination location or IATA code") @RequestParam(required = false) String destination) {
        return ResponseEntity.ok(searchRoutes.execute(origin, destination));
    }
}