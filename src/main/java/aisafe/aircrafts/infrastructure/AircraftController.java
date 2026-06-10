package aisafe.aircrafts.infrastructure;

import aisafe.aircrafts.application.*;
import aisafe.aircrafts.application.dtos.*;
import aisafe.aircrafts.domain.AircraftStatus;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.shared.domain.PaginatedResult;
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
    private final UpdateAircraftStatusUseCase updateAircraftStatus;
    private final DeleteAircraftUseCase deleteAircraft;
    private final UpdateAircraftUseCase updateAircraftUseCase;
    private final ViewCompatibleRoutesUseCase viewCompatibleRoutes;
    private final CalculateAircraftOperationalHoursUseCase calculateAircraftOperationalHours;
    private final GetAircraftUtilizationUseCase getAircraftUtilization;

    public AircraftController(ViewAircraftDetailsUseCase viewAircraftDetails, ListAircraftUseCase listAircraft,
                              RegisterAircraftUseCase registerAircraft, SearchAircraftUseCase searchAircraft,
                              UpdateAircraftStatusUseCase updateAircraftStatus,
                              DeleteAircraftUseCase deleteAircraft, UpdateAircraftUseCase updateAircraftUseCase,
                              ViewCompatibleRoutesUseCase viewCompatibleRoutes,
                              CalculateAircraftOperationalHoursUseCase calculateAircraftOperationalHours,
                              GetAircraftUtilizationUseCase getAircraftUtilization) {
        this.viewAircraftDetails = viewAircraftDetails;
        this.listAircraft = listAircraft;
        this.registerAircraft = registerAircraft;
        this.searchAircraft = searchAircraft;
        this.updateAircraftStatus = updateAircraftStatus;
        this.deleteAircraft = deleteAircraft;
        this.updateAircraftUseCase = updateAircraftUseCase;
        this.viewCompatibleRoutes = viewCompatibleRoutes;
        this.calculateAircraftOperationalHours = calculateAircraftOperationalHours;
        this.getAircraftUtilization = getAircraftUtilization;
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

    @Operation(summary = "Get all aircrafts with pagination", description = "Retrieves a paginated list of all aircrafts in the fleet along with their real-time availability status. (US205)")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<ListAircraftsUseCaseResponse>>> getAllAircraft(
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<ListAircraftsUseCaseResponse> assembler) {

        PaginatedResult<ListAircraftsUseCaseResponse> result = listAircraft.execute(
                pageable.getPageNumber(),
                pageable.getPageSize()
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
        ViewAircraftDetailsResponse aircraft = viewAircraftDetails.execute(registration);
        return ResponseEntity.ok(toHateoasModel(aircraft, registration));
    }

    @Operation(summary = "Search and filter aircrafts", description = "Advanced search that filters aircraft profiles dynamically by model name, current status, year of manufacturing, or specific feature with pagination support. Supports ATCC real-time status viewing. (US104, US205)")
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

    @Operation(summary = "Update aircraft operational status", description = "Updates the status of an existing aircraft. Requires the 'If-Match' header specifying the current resource version to perform Optimistic Concurrency Locking check. (US105)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aircraft status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status configuration or missing required parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Aircraft profile not found"),
            @ApiResponse(responseCode = "409", description = "Conflict detected -- The resource version has changed or matches a concurrency collision state")
    })
    @PatchMapping("/{registrationStr}/status")
    public ResponseEntity<EntityModel<ViewAircraftDetailsResponse>> updateAircraftStatus(
            @Parameter(description = "Registration identification key of the target aircraft") @PathVariable String registrationStr,
            @Parameter(description = "Current version entity state identifier for locking assessment")
            @RequestHeader(value = "If-Match", required = false) String ifMatchHeader,
            @Valid @RequestBody UpdateStatusRequest request) {

        Long version = ETagUtils.parseVersion(ifMatchHeader);
        RegistrationNumber registration = new RegistrationNumber(registrationStr);

        ViewAircraftDetailsResponse updatedAircraft = updateAircraftStatus.execute(registration, String.valueOf(request.status()), version);
        return ResponseEntity.ok(toHateoasModel(updatedAircraft, registration));
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

    @Operation(summary = "Update aircraft details", description = "Updates the technical details of an existing aircraft.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aircraft details updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data supplied"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Aircraft not found with specified registration number"),
            @ApiResponse(responseCode = "409", description = "Conflict detected -- The resource version has changed or matches a concurrency collision state")
    })
    @PatchMapping("/{registrationStr}")
    public ResponseEntity<EntityModel<ViewAircraftDetailsResponse>> updateAircraftDetails(
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
    public ResponseEntity<List<CompatibleRouteResponse>> getCompatibleRoutes(
            @Parameter(description = "Unique registration number code of the aircraft (e.g. CS-TKA)")
            @PathVariable String registrationStr) {

        RegistrationNumber registration = new RegistrationNumber(registrationStr);
        List<CompatibleRouteResponse> routes = viewCompatibleRoutes.execute(registration);
        return ResponseEntity.ok(routes);
    }

    @Operation(summary = "Calculate total operational hours", description = "Calculates the total operational hours for a specific aircraft based on completed flights. (US206)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total operational hours calculated successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Aircraft not found with specified registration number")
    })
    @GetMapping("/{registrationStr}/operational-hours")
    public ResponseEntity<AircraftOperationalHoursResponse> getOperationalHours(
            @Parameter(description = "Unique registration number code of the aircraft (e.g. CS-TKA)")
            @PathVariable String registrationStr) {

        RegistrationNumber registration = new RegistrationNumber(registrationStr);
        AircraftOperationalHoursResponse response = calculateAircraftOperationalHours.execute(registration);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get aircraft utilization rates over time", description = "Returns daily flight hours and utilization percentage for an aircraft in a given date range. (US223)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilization rates returned successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Aircraft not found with specified registration number")
    })
    @GetMapping("/{registrationStr}/utilization")
    public ResponseEntity<List<UtilizationDataPointResponse>> getAircraftUtilization(
            @Parameter(description = "Unique registration number code of the aircraft (e.g. CS-TKA)") @PathVariable String registrationStr,
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam java.time.LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam java.time.LocalDate endDate) {

        List<UtilizationDataPointResponse> response = getAircraftUtilization.execute(registrationStr, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    private EntityModel<ViewAircraftDetailsResponse> toHateoasModel(ViewAircraftDetailsResponse response, RegistrationNumber registration) {
        EntityModel<ViewAircraftDetailsResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(AircraftController.class).getAircraftByRegistrationNumber(registration.getNumber())).withSelfRel());
        model.add(linkTo(methodOn(AircraftController.class).getAllAircraft(Pageable.unpaged(), null)).withRel("all-aircrafts"));
        model.add(linkTo(methodOn(AircraftController.class).updateAircraftStatus(registration.getNumber(), null, null)).withRel("update-status"));
        return model;
    }

}