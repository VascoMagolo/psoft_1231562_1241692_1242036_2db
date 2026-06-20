package aisafe.aircrafts.infrastructure;

import aisafe.aircrafts.application.*;
import aisafe.aircrafts.application.dtos.*;
import aisafe.aircrafts.domain.AircraftStatus;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.shared.application.dtos.BulkImportResult;
import aisafe.shared.domain.PaginatedResult;
import aisafe.shared.infrastructure.BulkImportResponseBuilder;
import aisafe.shared.infrastructure.ETagUtils;
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
import aisafe.routes.infrastructure.RouteController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST controller for managing aircraft profiles in the system.
 * Provides endpoints for creating, retrieving, searching, and updating aircraft information.
 */
@RestController
@RequestMapping("/api/aircrafts")
@Tag(name = "Aircrafts", description = "Aircraft management - WP#1A")
public class AircraftController {

    private final ViewAircraftDetailsUseCase viewAircraftDetails;
    private final ListAircraftUseCase listAircraft;
    private final RegisterAircraftUseCase registerAircraft;
    private final SearchAircraftUseCase searchAircraft;
    private final DeleteAircraftUseCase deleteAircraft;
    private final UpdateAircraftUseCase updateAircraftUseCase;
    private final ViewCompatibleRoutesUseCase viewCompatibleRoutes;
    private final CalculateAircraftOperationalHoursUseCase calculateAircraftOperationalHours;
    private final GetAircraftUtilizationUseCase getAircraftUtilization;
    private final CalculateFuelEfficiencyUseCase calculateFuelEfficiency;
    private final ViewFleetStatusUseCase viewFleetStatus;
    private final ImportAircraftsUseCase importAircrafts;

    public AircraftController(ViewAircraftDetailsUseCase viewAircraftDetails, ListAircraftUseCase listAircraft,
                              RegisterAircraftUseCase registerAircraft, SearchAircraftUseCase searchAircraft,
                              DeleteAircraftUseCase deleteAircraft, UpdateAircraftUseCase updateAircraftUseCase,
                              ViewCompatibleRoutesUseCase viewCompatibleRoutes,
                              CalculateAircraftOperationalHoursUseCase calculateAircraftOperationalHours,
                              GetAircraftUtilizationUseCase getAircraftUtilization,
                              CalculateFuelEfficiencyUseCase calculateFuelEfficiency,
                              ViewFleetStatusUseCase viewFleetStatus,
                              ImportAircraftsUseCase importAircrafts) {
        this.viewAircraftDetails = viewAircraftDetails;
        this.listAircraft = listAircraft;
        this.registerAircraft = registerAircraft;
        this.searchAircraft = searchAircraft;
        this.deleteAircraft = deleteAircraft;
        this.updateAircraftUseCase = updateAircraftUseCase;
        this.viewCompatibleRoutes = viewCompatibleRoutes;
        this.calculateAircraftOperationalHours = calculateAircraftOperationalHours;
        this.getAircraftUtilization = getAircraftUtilization;
        this.calculateFuelEfficiency = calculateFuelEfficiency;
        this.viewFleetStatus = viewFleetStatus;
        this.importAircrafts = importAircrafts;
    }

    @Operation(summary = "Bulk import aircrafts via CSV")
    @PostMapping(value = "/import", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<java.util.Map<String, Object>> importAircrafts(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        BulkImportResult<ViewAircraftDetailsResponse> result = importAircrafts.execute(file);
        return BulkImportResponseBuilder.buildResponse(result);
    }

    @Operation(summary = "Register a new aircraft", description = "Creates a new aircraft profile configuration in the system. Requires Fleet Manager role. (US102)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Aircraft successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid request data supplied"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "409", description = "Aircraft with given registration number already exists")
    })
    @PostMapping
    public ResponseEntity<EntityModel<ViewAircraftDetailsResponse>> registerAircraft(
            @Valid @RequestBody RegisterAircraftRequest request) {

        ViewAircraftDetailsResponse createdAircraft = registerAircraft.execute(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toHateoasModel(createdAircraft, new RegistrationNumber(request.registrationNumber())));
    }

    @Operation(summary = "Get all aircrafts with pagination", description = "Retrieves a paginated list of all aircrafts in the fleet.")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<ListAircraftsUseCaseResponse>>> getAllAircraft(
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<ListAircraftsUseCaseResponse> assembler) {

        PaginatedResult<ListAircraftsUseCaseResponse> result = listAircraft.execute(
                new ListAircraftRequest(pageable.getPageNumber(), pageable.getPageSize())
        );

        Page<ListAircraftsUseCaseResponse> aircraftPage = new PageImpl<>(
                result.data(),
                pageable,
                result.totalElements()
        );
        PagedModel<EntityModel<ListAircraftsUseCaseResponse>> pagedModel =
                assembler.toModel(aircraftPage, aircraft -> EntityModel.of(aircraft)
                        .add(linkTo(methodOn(AircraftController.class)
                                .getAircraftByRegistrationNumber(aircraft.registrationNumber()))
                                .withSelfRel()));

        return ResponseEntity.ok(pagedModel);
    }

    @Operation(summary = "Get aircraft details by registration number", description = "Returns complete technical and operational details for a specific aircraft using its unique registration identifier. (US103)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aircraft details found and returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Aircraft not found with specified registration number")
    })
    @GetMapping("/{registrationStr}")
    public ResponseEntity<EntityModel<ViewAircraftDetailsResponse>> getAircraftByRegistrationNumber(
            @Parameter(description = "Unique registration number code of the aircraft (e.g. CS-TKA)")
            @PathVariable String registrationStr) {

        RegistrationNumber registration = new RegistrationNumber(registrationStr);
        ViewAircraftDetailsResponse aircraft = viewAircraftDetails.execute(new ViewAircraftDetailsRequest(registration));
        return ResponseEntity.ok(toHateoasModel(aircraft, registration));
    }

    @Operation(summary = "Search and filter aircrafts", description = "Advanced search that filters aircraft profiles dynamically by model name, current status, year of manufacturing, or specific feature with pagination support. (US104, US224)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results returned successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @GetMapping("/search")
    public ResponseEntity<PagedModel<EntityModel<SearchAircraftUseCaseResponse>>> searchAircrafts(
            @Parameter(description = "Filter by technical model name") @RequestParam(required = false) String modelName,
            @Parameter(description = "Filter by aircraft current operational status") @RequestParam(required = false) AircraftStatus status,
            @Parameter(description = "Filter by the exact year the aircraft was manufactured") @RequestParam(required = false) Integer year,
            @Parameter(description = "Filter by a specific feature (e.g., 'WiFi')") @RequestParam(required = false) String feature,
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<SearchAircraftUseCaseResponse> assembler) {

        String statusStr = status != null ? status.name() : null;

        PaginatedResult<SearchAircraftUseCaseResponse> result = searchAircraft.execute(
                modelName, statusStr, year, feature, pageable.getPageNumber(), pageable.getPageSize()
        );

        Page<SearchAircraftUseCaseResponse> resultsPage = new PageImpl<>(
                result.data(),
                pageable,
                result.totalElements()
        );

        PagedModel<EntityModel<SearchAircraftUseCaseResponse>> pagedModel =
                assembler.toModel(resultsPage, aircraft -> EntityModel.of(aircraft)
                        .add(linkTo(methodOn(AircraftController.class)
                                .getAircraftByRegistrationNumber(aircraft.registrationNumber()))
                                .withSelfRel()));

        return ResponseEntity.ok(pagedModel);
    }

    @Operation(summary = "Delete an aircraft", description = "Permanently removes an aircraft by registration number. Requires ATCC or Admin role.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Aircraft deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Aircraft not found")
    })
    @DeleteMapping("/{registrationStr}")
    public ResponseEntity<Void> deleteAircraft(
            @Parameter(description = "Unique registration number of the aircraft (e.g. CS-TKA)")
            @PathVariable String registrationStr) {

        RegistrationNumber registration = new RegistrationNumber(registrationStr);
        deleteAircraft.execute(registration);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update aircraft details and status", description = "Updates the technical details and/or the operational status of an existing aircraft. Requires the 'If-Match' header specifying the current resource version to perform Optimistic Concurrency Locking check. (US105)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aircraft details updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data supplied"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Aircraft not found with specified registration number"),
            @ApiResponse(responseCode = "409", description = "Conflict detected -- The resource version has changed or matches a concurrency collision state")
    })
    @PatchMapping("/{registrationStr}")
    public ResponseEntity<EntityModel<ViewAircraftDetailsResponse>> updateAircraft(
            @PathVariable String registrationStr,
            @RequestHeader(value = "If-Match", required = false) String ifMatchHeader,
            @RequestBody UpdateAircraftRequest request) {

        Long version = ETagUtils.parseVersion(ifMatchHeader);
        RegistrationNumber registration = new RegistrationNumber(registrationStr);
        ViewAircraftDetailsResponse response = updateAircraftUseCase.execute(registration, request, version);
        return ResponseEntity.ok(toHateoasModel(response, registration));
    }

    @Operation(summary = "Get compatible routes for an aircraft", description = "Returns a list of active routes compatible with the aircraft based on its range and capacity. (US203)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compatible routes retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Aircraft not found with specified registration number")
    })
    @GetMapping("/{registrationStr}/compatible-routes")
    public ResponseEntity<CollectionModel<EntityModel<CompatibleRouteResponse>>> getCompatibleRoutes(
            @Parameter(description = "Unique registration number code of the aircraft (e.g. CS-TKA)")
            @PathVariable String registrationStr) {

        RegistrationNumber registration = new RegistrationNumber(registrationStr);
        List<EntityModel<CompatibleRouteResponse>> items = viewCompatibleRoutes.execute(registration).stream()
                .map(r -> EntityModel.of(r,
                        linkTo(methodOn(RouteController.class).getRouteDetails(r.originIataCode(), r.destinationIataCode())).withRel("route")))
                .toList();
        CollectionModel<EntityModel<CompatibleRouteResponse>> model = CollectionModel.of(items,
                linkTo(methodOn(AircraftController.class).getCompatibleRoutes(registrationStr)).withSelfRel(),
                linkTo(methodOn(AircraftController.class).getAircraftByRegistrationNumber(registrationStr)).withRel("aircraft"));
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Calculate total operational hours", description = "Calculates the total operational hours for a specific aircraft based on completed flights. (US206)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total operational hours calculated successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Aircraft not found with specified registration number")
    })
    @GetMapping("/{registrationStr}/operational-hours")
    public ResponseEntity<EntityModel<AircraftOperationalHoursResponse>> getOperationalHours(
            @Parameter(description = "Unique registration number code of the aircraft (e.g. CS-TKA)")
            @PathVariable String registrationStr) {

        RegistrationNumber registration = new RegistrationNumber(registrationStr);
        AircraftOperationalHoursResponse response = calculateAircraftOperationalHours.execute(new CalculateAircraftOperationalHoursRequest(registration));
        return ResponseEntity.ok(EntityModel.of(response,
                linkTo(methodOn(AircraftController.class).getOperationalHours(registrationStr)).withSelfRel(),
                linkTo(methodOn(AircraftController.class).getAircraftByRegistrationNumber(registrationStr)).withRel("aircraft")));
    }

    @Operation(summary = "Get aircraft utilization rates over time", description = "Returns daily flight hours and utilization percentage for an aircraft in a given date range. (US223)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilization rates returned successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Aircraft not found with specified registration number")
    })
    @GetMapping("/{registrationStr}/utilization")
    public ResponseEntity<CollectionModel<EntityModel<UtilizationDataPointResponse>>> getAircraftUtilization(
            @Parameter(description = "Unique registration number code of the aircraft (e.g. CS-TKA)") @PathVariable String registrationStr,
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam java.time.LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam java.time.LocalDate endDate) {

        List<EntityModel<UtilizationDataPointResponse>> items = getAircraftUtilization.execute(new GetAircraftUtilizationRequest(registrationStr, startDate, endDate)).stream()
                .map(EntityModel::of)
                .toList();
        CollectionModel<EntityModel<UtilizationDataPointResponse>> model = CollectionModel.of(items,
                linkTo(methodOn(AircraftController.class).getAircraftUtilization(registrationStr, startDate, endDate)).withSelfRel(),
                linkTo(methodOn(AircraftController.class).getAircraftByRegistrationNumber(registrationStr)).withRel("aircraft"));
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Calculate fuel efficiency", description = "Calculates fuel efficiency metrics per aircraft and per route. (US227)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fuel efficiency calculated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid route ID supplied"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Aircraft not found with specified registration number")
    })
    @GetMapping("/{registrationStr}/fuel-efficiency")
    public ResponseEntity<EntityModel<FuelEfficiencyResponse>> getFuelEfficiency(
            @Parameter(description = "Unique registration number code of the aircraft (e.g. CS-TKA)") @PathVariable String registrationStr,
            @Parameter(description = "Optional origin IATA code to calculate specific fuel needs") @RequestParam(required = false) String origin,
            @Parameter(description = "Optional destination IATA code to calculate specific fuel needs") @RequestParam(required = false) String destination) {

        FuelEfficiencyResponse response = calculateFuelEfficiency.execute(new CalculateFuelEfficiencyRequest(registrationStr, origin, destination));
        return ResponseEntity.ok(EntityModel.of(response,
                linkTo(methodOn(AircraftController.class).getFuelEfficiency(registrationStr, origin, destination)).withSelfRel(),
                linkTo(methodOn(AircraftController.class).getAircraftByRegistrationNumber(registrationStr)).withRel("aircraft")));
    }

    @Operation(summary = "View fleet status overview", description = "Returns all aircraft grouped by their current operational status (AVAILABLE, UNDER_MAINTENANCE, IN_FLIGHT, INACTIVE). Provides a real-time fleet-wide availability snapshot. (US205)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fleet status overview returned successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @GetMapping("/fleet-status")
    public ResponseEntity<EntityModel<FleetStatusResponse>> getFleetStatus() {
        FleetStatusResponse response = viewFleetStatus.execute();
        EntityModel<FleetStatusResponse> model = EntityModel.of(response,
                linkTo(methodOn(AircraftController.class).getFleetStatus()).withSelfRel(),
                linkTo(methodOn(AircraftController.class).getAllAircraft(Pageable.unpaged(), null)).withRel("all-aircrafts"));
        return ResponseEntity.ok(model);
    }

    private EntityModel<ViewAircraftDetailsResponse> toHateoasModel(ViewAircraftDetailsResponse response, RegistrationNumber registration) {
        EntityModel<ViewAircraftDetailsResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(AircraftController.class).getAircraftByRegistrationNumber(registration.getNumber())).withSelfRel());
        model.add(linkTo(methodOn(AircraftController.class).getAllAircraft(Pageable.unpaged(), null)).withRel("all-aircrafts"));
        model.add(linkTo(methodOn(AircraftController.class).updateAircraft(registration.getNumber(), null, null)).withRel("update-aircraft"));
        return model;
    }

}
