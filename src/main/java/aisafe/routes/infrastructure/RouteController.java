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
import aisafe.shared.domain.PaginatedResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

import aisafe.shared.infrastructure.ETagUtils;

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
    private final DeleteRouteUseCase deleteRoute;

    public RouteController(CreateRouteUseCase createRoute,
                           ViewRouteHistoryUseCase viewRouteHistory,
                           UpdateRouteUseCase updateRoute,
                           DesactivateRouteUseCase desactivateRoute,
                           ViewRouteDetailsUseCase viewRouteDetails,
                           ListRoutesFromAirportUseCase listRoutesFromAirport,
                           SearchRoutesUseCase searchRoutes,
                           DeleteRouteUseCase deleteRoute) {
        this.createRoute = createRoute;
        this.viewRouteHistory = viewRouteHistory;
        this.updateRoute = updateRoute;
        this.desactivateRoute = desactivateRoute;
        this.viewRouteDetails = viewRouteDetails;
        this.listRoutesFromAirport = listRoutesFromAirport;
        this.searchRoutes = searchRoutes;
        this.deleteRoute = deleteRoute;
    }

    private EntityModel<RouteResponse> toModel(RouteResponse route) {
        String origin = route.originIataCode();
        String destination = route.destinationIataCode();
        return EntityModel.of(route,
                linkTo(methodOn(RouteController.class).getRouteDetails(origin, destination)).withSelfRel(),
                linkTo(methodOn(RouteController.class).getRouteHistory(origin, destination)).withRel("history"),
                linkTo(methodOn(RouteController.class).updateRoute(origin, destination, null, null)).withRel("update"),
                linkTo(methodOn(RouteController.class).deactivateRoute(origin, destination, null)).withRel("deactivate"),
                linkTo(methodOn(RouteController.class).deleteRoute(origin, destination)).withRel("delete"));
    }

    private EntityModel<RouteResponse> mapToModel(Route r) {
        RouteResponse response = new RouteResponse(
                r.getOrigin().getCode(),
                r.getDestination().getCode(),
                r.getEstimatedFlightTime(),
                r.getMinimumRange(),
                r.getMinimumCapacity(),
                r.getStatus());
        return toModel(response);
    }

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

    // US111
    @Operation(summary = "Keep track of route history", description = "Retrieves the historical changes and updates made to a specific route. (US111)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Route history retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Route not found")
    })
    @GetMapping("/{origin}/{destination}/history")
    public ResponseEntity<CollectionModel<EntityModel<RouteHistoryResponse>>> getRouteHistory(
            @Parameter(description = "IATA code of the origin airport") @PathVariable String origin,
            @Parameter(description = "IATA code of the destination airport") @PathVariable String destination) {
        String originUpper = origin.toUpperCase();
        String destinationUpper = destination.toUpperCase();
        List<EntityModel<RouteHistoryResponse>> historyModels = viewRouteHistory.execute(originUpper, destinationUpper).stream()
                .map(h -> {
                    RouteHistoryResponse response = new RouteHistoryResponse(
                            h.getOriginCode(), h.getDestinationCode(),
                            h.getChangeDescription(), h.getChangedAt(), h.getChangedBy());
                    return EntityModel.of(response,
                            linkTo(methodOn(RouteController.class).getRouteDetails(originUpper, destinationUpper))
                                    .withRel("route"));
                })
                .toList();
        return ResponseEntity.ok(CollectionModel.of(historyModels,
                linkTo(methodOn(RouteController.class).getRouteHistory(originUpper, destinationUpper)).withSelfRel(),
                linkTo(methodOn(RouteController.class).getRouteDetails(originUpper, destinationUpper)).withRel("route")));
    }

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
    @PutMapping("/{origin}/{destination}")
    public ResponseEntity<EntityModel<RouteResponse>> updateRoute(
            @Parameter(description = "IATA code of the origin airport") @PathVariable String origin,
            @Parameter(description = "IATA code of the destination airport") @PathVariable String destination,
            @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch,
            @Valid @RequestBody UpdateRouteRequest request) {
        Long version = ETagUtils.parseVersion(ifMatch);
        return ResponseEntity.ok(mapToModel(updateRoute.execute(origin.toUpperCase(), destination.toUpperCase(), request, version)));
    }

    // US112
    @Operation(summary = "Deactivate a route", description = "Sets an active flight route status to deactivated. (US112)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Route deactivated successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Route not found"),
            @ApiResponse(responseCode = "412", description = "ETag validation failed")
    })
    @PatchMapping("/{origin}/{destination}/deactivate")
    public ResponseEntity<EntityModel<RouteResponse>> deactivateRoute(
            @Parameter(description = "IATA code of the origin airport") @PathVariable String origin,
            @Parameter(description = "IATA code of the destination airport") @PathVariable String destination,
            @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        Long version = ETagUtils.parseVersion(ifMatch);
        return ResponseEntity.ok(mapToModel(desactivateRoute.execute(origin.toUpperCase(), destination.toUpperCase(), version)));
    }

    // US113
    @Operation(summary = "View route details", description = "Retrieves the full details of a specific route. (US113)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Route details retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Route not found")
    })
    @GetMapping("/{origin}/{destination}")
    public ResponseEntity<EntityModel<RouteResponse>> getRouteDetails(
            @Parameter(description = "IATA code of the origin airport") @PathVariable String origin,
            @Parameter(description = "IATA code of the destination airport") @PathVariable String destination) {
        return ResponseEntity.ok(mapToModel(viewRouteDetails.execute(origin.toUpperCase(), destination.toUpperCase())));
    }

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
        PaginatedResult<Route> result = listRoutesFromAirport.execute(iataCode.toUpperCase(), pageable.getPageNumber(), pageable.getPageSize());
        Page<Route> routePage = new PageImpl<>(result.data(), pageable, result.totalElements());
        return ResponseEntity.ok(assembler.toModel(routePage, this::mapToModel));
    }

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
            @Parameter(description = "Origin IATA code") @RequestParam(required = false) String origin,
            @Parameter(description = "Destination IATA code") @RequestParam(required = false) String destination,
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<Route> assembler) {
        PaginatedResult<Route> result = this.searchRoutes.execute(origin, destination, pageable.getPageNumber(), pageable.getPageSize());
        Page<Route> routePage = new PageImpl<>(result.data(), pageable, result.totalElements());
        return ResponseEntity.ok(assembler.toModel(routePage, this::mapToModel));
    }

    @Operation(summary = "Delete a route", description = "Permanently removes a flight route. Requires Admin role.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Route deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Route not found")
    })
    @DeleteMapping("/{origin}/{destination}")
    public ResponseEntity<Void> deleteRoute(
            @Parameter(description = "IATA code of the origin airport") @PathVariable String origin,
            @Parameter(description = "IATA code of the destination airport") @PathVariable String destination) {
        deleteRoute.execute(origin.toUpperCase(), destination.toUpperCase());
        return ResponseEntity.noContent().build();
    }
}
