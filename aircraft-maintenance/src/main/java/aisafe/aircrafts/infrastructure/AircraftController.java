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
import org.springframework.beans.factory.annotation.Value;
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
    private final CalculateAircraftOperationalHoursUseCase calculateAircraftOperationalHours;
    private final GetAircraftUtilizationUseCase getAircraftUtilization;
    private final ViewFleetStatusUseCase viewFleetStatus;
    private final ImportAircraftsUseCase importAircrafts;
    /** Which side answers the listing: the CQRS read model with events on, the peers without. */
    private final String querySide;

    public AircraftController(ViewAircraftDetailsUseCase viewAircraftDetails, ListAircraftUseCase listAircraft,
                              RegisterAircraftUseCase registerAircraft, SearchAircraftUseCase searchAircraft,
                              DeleteAircraftUseCase deleteAircraft, UpdateAircraftUseCase updateAircraftUseCase,
                              CalculateAircraftOperationalHoursUseCase calculateAircraftOperationalHours,
                              GetAircraftUtilizationUseCase getAircraftUtilization,
                              ViewFleetStatusUseCase viewFleetStatus,
                              ImportAircraftsUseCase importAircrafts,
                              @Value("${aisafe.events.enabled:false}") boolean eventsEnabled) {
        this.viewAircraftDetails = viewAircraftDetails;
        this.listAircraft = listAircraft;
        this.registerAircraft = registerAircraft;
        this.searchAircraft = searchAircraft;
        this.deleteAircraft = deleteAircraft;
        this.updateAircraftUseCase = updateAircraftUseCase;
        this.calculateAircraftOperationalHours = calculateAircraftOperationalHours;
        this.getAircraftUtilization = getAircraftUtilization;
        this.viewFleetStatus = viewFleetStatus;
        this.importAircrafts = importAircrafts;
        this.querySide = eventsEnabled ? "read-model" : "peers";
    }

    @Operation(summary = "Bulk import aircrafts via CSV")
    @PostMapping(value = "/import", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<java.util.Map<String, Object>> importAircrafts(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        BulkImportResult<AircraftResponse> result = importAircrafts.execute(file);
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
    public ResponseEntity<EntityModel<AircraftResponse>> registerAircraft(
            @Valid @RequestBody RegisterAircraftRequest request) {

        AircraftResponse createdAircraft = registerAircraft.execute(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toHateoasModel(createdAircraft, new RegistrationNumber(request.registrationNumber())));
    }

    @Operation(summary = "Get all aircrafts with pagination", description = "Retrieves a paginated list of all aircrafts in the fleet.")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<AircraftResponse>>> getAllAircraft(
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<AircraftResponse> assembler) {

        PaginatedResult<AircraftResponse> result = listAircraft.execute(
                new ListAircraftRequest(pageable.getPageNumber(), pageable.getPageSize())
        );

        Page<AircraftResponse> aircraftPage = new PageImpl<>(
                result.data(),
                pageable,
                result.totalElements()
        );
        PagedModel<EntityModel<AircraftResponse>> pagedModel =
                assembler.toModel(aircraftPage, aircraft -> EntityModel.of(aircraft)
                        .add(linkTo(methodOn(AircraftController.class)
                                .getAircraftByRegistrationNumber(aircraft.registrationNumber()))
                                .withSelfRel()));

        return ResponseEntity.ok().header("X-Query-Side", querySide).body(pagedModel);
    }

    @Operation(summary = "Get aircraft details by registration number", description = "Returns complete technical and operational details for a specific aircraft using its unique registration identifier. (US103)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aircraft details found and returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Aircraft not found with specified registration number")
    })
    @GetMapping("/{registrationStr}")
    public ResponseEntity<EntityModel<AircraftResponse>> getAircraftByRegistrationNumber(
            @Parameter(description = "Unique registration number code of the aircraft (e.g. CS-TKA)")
            @PathVariable String registrationStr) {

        RegistrationNumber registration = new RegistrationNumber(registrationStr);
        AircraftResponse aircraft = viewAircraftDetails.execute(new ViewAircraftDetailsRequest(registration));
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
    public ResponseEntity<EntityModel<AircraftResponse>> updateAircraft(
            @PathVariable String registrationStr,
            @RequestHeader(value = "If-Match", required = false) String ifMatchHeader,
            @RequestBody UpdateAircraftRequest request) {

        Long version = ETagUtils.parseVersion(ifMatchHeader);
        RegistrationNumber registration = new RegistrationNumber(registrationStr);
        AircraftResponse response = updateAircraftUseCase.execute(registration, request, version);
        return ResponseEntity.ok(toHateoasModel(response, registration));
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

    private EntityModel<AircraftResponse> toHateoasModel(AircraftResponse response, RegistrationNumber registration) {
        EntityModel<AircraftResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(AircraftController.class).getAircraftByRegistrationNumber(registration.getNumber())).withSelfRel());
        model.add(linkTo(methodOn(AircraftController.class).getAllAircraft(Pageable.unpaged(), null)).withRel("all-aircrafts"));
        model.add(linkTo(methodOn(AircraftController.class).updateAircraft(registration.getNumber(), null, null)).withRel("update-aircraft"));
        return model;
    }

}
