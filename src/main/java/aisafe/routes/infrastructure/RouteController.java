package aisafe.routes.infrastructure;

import aisafe.routes.application.*;
import aisafe.routes.application.dtos.ActiveRouteResponse;
import aisafe.routes.application.dtos.AlternativeRouteResponse;
import aisafe.routes.application.dtos.CreateRouteRequest;
import aisafe.shared.application.ExportedFile;
import aisafe.routes.application.dtos.RouteHistoryResponse;
import aisafe.routes.application.dtos.RouteResponse;
import aisafe.routes.application.dtos.UpdateRouteRequest;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
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
    private final DeactivateRouteUseCase deactivateRoute;
    private final ViewRouteDetailsUseCase viewRouteDetails;
    private final ListRoutesFromAirportUseCase listRoutesFromAirport;
    private final SearchRoutesUseCase searchRoutes;
    private final DeleteRouteUseCase deleteRoute;
    private final RouteRepository routeRepository;
    private final ListActiveRoutesUseCase listActiveRoutes;
    private final SearchAlternativeRoutesUseCase searchAlternativeRoutes;
    private final ExportRouteNetworkUseCase exportRouteNetwork;

    public RouteController(CreateRouteUseCase createRoute,
                           ViewRouteHistoryUseCase viewRouteHistory,
                           UpdateRouteUseCase updateRoute,
                           DeactivateRouteUseCase deactivateRoute,
                           ViewRouteDetailsUseCase viewRouteDetails,
                           ListRoutesFromAirportUseCase listRoutesFromAirport,
                           SearchRoutesUseCase searchRoutes,
                           DeleteRouteUseCase deleteRoute,
                           RouteRepository routeRepository,
                           ListActiveRoutesUseCase listActiveRoutes,
                           SearchAlternativeRoutesUseCase searchAlternativeRoutes,
                           ExportRouteNetworkUseCase exportRouteNetwork) {
        this.createRoute = createRoute;
        this.viewRouteHistory = viewRouteHistory;
        this.updateRoute = updateRoute;
        this.deactivateRoute = deactivateRoute;
        this.viewRouteDetails = viewRouteDetails;
        this.listRoutesFromAirport = listRoutesFromAirport;
        this.searchRoutes = searchRoutes;
        this.deleteRoute = deleteRoute;
        this.routeRepository = routeRepository;
        this.listActiveRoutes = listActiveRoutes;
        this.searchAlternativeRoutes = searchAlternativeRoutes;
        this.exportRouteNetwork = exportRouteNetwork;
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
        Long version = routeRepository.findVersionFor(r.getOrigin(), r.getDestination());
        RouteResponse response = new RouteResponse(
                r.getOrigin().getCode(),
                r.getDestination().getCode(),
                r.getEstimatedFlightTime(),
                r.getMinimumRange(),
                r.getMinimumCapacity(),
                r.getStatus(),
                version);
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
        return ResponseEntity.status(HttpStatus.CREATED).body(toModel(createRoute.execute(request)));
    }

    // US214
    @Operation(summary = "List active routes", description = "Lists active routes sorted by distance or popularity. (US214)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active routes retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status or sortBy parameter")
    })
    @GetMapping
    public ResponseEntity<CollectionModel<ActiveRouteResponse>> getActiveRoutes(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy) {
        return ResponseEntity.ok(CollectionModel.of(
                listActiveRoutes.execute(status, sortBy),
                linkTo(methodOn(RouteController.class).getActiveRoutes(status, sortBy)).withSelfRel()));
    }

    // US216
    @Operation(summary = "Search alternative routes", description = "Finds indirect active route paths between two airports. (US216)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alternative routes retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid origin or destination"),
            @ApiResponse(responseCode = "404", description = "Airport not found")
    })
    @GetMapping("/alternatives")
    public ResponseEntity<CollectionModel<AlternativeRouteResponse>> getAlternativeRoutes(
            @RequestParam String origin,
            @RequestParam String destination) {
        return ResponseEntity.ok(CollectionModel.of(
                searchAlternativeRoutes.execute(origin, destination),
                linkTo(methodOn(RouteController.class).getAlternativeRoutes(origin, destination)).withSelfRel()));
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
                .map(response -> EntityModel.of(response,
                            linkTo(methodOn(RouteController.class).getRouteDetails(originUpper, destinationUpper))
                                    .withRel("route")))
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
        return ResponseEntity.ok(mapToModel(deactivateRoute.execute(origin.toUpperCase(), destination.toUpperCase(), version)));
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
        return ResponseEntity.ok(toModel(viewRouteDetails.execute(origin.toUpperCase(), destination.toUpperCase())));
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

    // US228
    @Operation(summary = "Export route network", description = "Exports the active route network in GeoJSON or KML format. (US228)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Export file generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid format requested"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportRouteNetwork(
            @RequestParam(defaultValue = "geojson") String format) {
        ExportedFile file = exportRouteNetwork.execute(format);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, file.contentType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.fileName() + "\"")
                .body(file.content());
    }
}
