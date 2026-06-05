package aisafe.maintenance.infrastructure;

import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.maintenance.application.*;
import aisafe.maintenance.application.dtos.*;
import aisafe.shared.domain.PaginatedResult;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST controller for managing aircraft maintenance operations
 */
@RestController
@RequestMapping("/api/maintenance")
@Tag(name = "Maintenance", description = "Aircraft Maintenance Management - WP#4")
public class MaintenanceController {

    private final CreateMaintenanceTemplateUseCase createMaintenanceTemplateUseCase;
    private final CreateMaintenanceRecordUseCase createMaintenanceRecordUseCase;
    private final CreateMaintenancePartUseCase createMaintenancePartUseCase;
    private final UpdateMaintenanceRecordUseCase updateMaintenanceRecordUseCase;
    private final ViewAllMaintenanceRecordsUseCase viewAllMaintenanceRecordsUseCase;
    private final ViewTotalMaintenanceHoursInFleetUseCase viewTotalMaintenanceHoursInFleetUseCase;
    private final DeleteMaintenanceRecordUseCase deleteMaintenanceRecordUseCase;
    private final DeleteMaintenanceTemplateUseCase deleteMaintenanceTemplateUseCase;
    private final DeleteMaintenancePartUseCase deleteMaintenancePartUseCase;

    public MaintenanceController(CreateMaintenanceTemplateUseCase createMaintenanceTemplateUseCase,
                                 CreateMaintenanceRecordUseCase createMaintenanceRecordUseCase,
                                 CreateMaintenancePartUseCase createMaintenancePartUseCase,
                                 UpdateMaintenanceRecordUseCase updateMaintenanceRecordUseCase,
                                 ViewAllMaintenanceRecordsUseCase viewAllMaintenanceRecordsUseCase,
                                 ViewTotalMaintenanceHoursInFleetUseCase viewTotalMaintenanceHoursInFleetUseCase,
                                 DeleteMaintenanceRecordUseCase deleteMaintenanceRecordUseCase,
                                 DeleteMaintenanceTemplateUseCase deleteMaintenanceTemplateUseCase,
                                 DeleteMaintenancePartUseCase deleteMaintenancePartUseCase) {
        this.createMaintenanceTemplateUseCase = createMaintenanceTemplateUseCase;
        this.createMaintenanceRecordUseCase = createMaintenanceRecordUseCase;
        this.createMaintenancePartUseCase = createMaintenancePartUseCase;
        this.updateMaintenanceRecordUseCase = updateMaintenanceRecordUseCase;
        this.viewAllMaintenanceRecordsUseCase = viewAllMaintenanceRecordsUseCase;
        this.viewTotalMaintenanceHoursInFleetUseCase = viewTotalMaintenanceHoursInFleetUseCase;
        this.deleteMaintenanceRecordUseCase = deleteMaintenanceRecordUseCase;
        this.deleteMaintenanceTemplateUseCase = deleteMaintenanceTemplateUseCase;
        this.deleteMaintenancePartUseCase = deleteMaintenancePartUseCase;
    }

    /**
     * Endpoint to create a new maintenance template configuration in the system.
     * @param request the request body containing the details of the maintenance template to be created
     * @return a ResponseEntity containing the created MaintenanceTemplateResponse
     */
    @Operation(summary = "Create maintenance template", description = "Registers a new maintenance template configuration in the system. (US115b)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Template created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data supplied"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PostMapping("/templates")
    public ResponseEntity<EntityModel<MaintenanceTemplateResponse>> createMaintenanceTemplate(@Valid @RequestBody CreateMaintenanceTemplateRequest request) {
        MaintenanceTemplateResponse response = createMaintenanceTemplateUseCase.execute(request);
        EntityModel<MaintenanceTemplateResponse> model = EntityModel.of(response,
                linkTo(methodOn(MaintenanceController.class).createMaintenanceRecord(null)).withRel("create-record"));
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    /**
     * Endpoint to register a new maintenance part in the system.
     * @param request the request body containing the details of the maintenance part to be registered
     * @return a ResponseEntity containing the created MaintenancePartResponse
     */
    @Operation(summary = "Register a maintenance part", description = "Adds a new hardware component or part to the maintenance catalog. (US226)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Part registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data supplied"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PostMapping("/parts")
    public ResponseEntity<EntityModel<MaintenancePartResponse>> createMaintenancePart(@Valid @RequestBody CreateMaintenancePartRequest request) {
        MaintenancePartResponse response = createMaintenancePartUseCase.execute(request);
        EntityModel<MaintenancePartResponse> model = EntityModel.of(response,
                linkTo(methodOn(MaintenanceController.class).createMaintenanceRecord(null)).withRel("create-record"));
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    /**
     * Endpoint to create a new maintenance record for a specific aircraft
     * @param request the request body containing the details of the maintenance record to be created
     * @return a ResponseEntity containing the created MaintenanceRecordResponse with HATEOAS links for further actions
     */
    @Operation(summary = "Create a maintenance record", description = "Schedules or logs a new maintenance record for a specific aircraft. (US115a)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Maintenance record created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data supplied"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Aircraft, template, or part not found")
    })
    @PostMapping("/records")
    public ResponseEntity<EntityModel<MaintenanceRecordResponse>> createMaintenanceRecord(
            @Valid @RequestBody CreateMaintenanceRecordRequest request) {

        MaintenanceRecordResponse response = createMaintenanceRecordUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toHateoasModel(response));
    }

    /**
     * Endpoint to update the operational status and notes of an existing maintenance record.
     * @param id the unique ID of the maintenance record to be updated
     * @param ifMatchHeader the value of the 'If-Match' header containing the current version of the resource for Optimistic Concurrency Locking check
     * @param request the request body containing the new status and notes for the maintenance record
     * @return a ResponseEntity containing the updated MaintenanceRecordResponse with HATEOAS links for further actions
     */
    @Operation(summary = "Update maintenance record", description = "Updates the operational status and notes of an existing maintenance record. Requires the 'If-Match' header specifying the current resource version to perform Optimistic Concurrency Locking check. (US119)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Record updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data or missing If-Match header"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Maintenance record not found"),
            @ApiResponse(responseCode = "409", description = "Conflict detected -- The resource version has changed or matches a concurrency collision state")
    })
    @PatchMapping("/records/{id}")
    public ResponseEntity<EntityModel<MaintenanceRecordResponse>> updateRecordStatusAndNotes(
            @Parameter(description = "Unique ID of the maintenance record") @PathVariable Long id,
            @Parameter(description = "Current version entity state identifier for locking assessment")
            @RequestHeader(value = "If-Match", required = false) String ifMatchHeader,
            @Valid @RequestBody UpdateMaintenanceRecordsRequest request) {
        Long version = parseEtagToVersion(ifMatchHeader);
        MaintenanceRecordResponse updatedRecord = updateMaintenanceRecordUseCase.execute(id, request, version);
        return ResponseEntity.ok(toHateoasModel(updatedRecord));
    }

    /**
     * Endpoint to retrieve all maintenance records associated with a specific aircraft registration number
     * @param registrationNumber the unique registration number code of the aircraft
     * @param pageable the pagination information for the request
     * @param assembler the PagedResourcesAssembler used to convert the Page of responses into a HATEOAS-compliant paged model
     * @return a ResponseEntity containing a paginated list of maintenance records for the specified aircraft
     */
    @Operation(summary = "Get maintenance records by aircraft", description = "Returns a paginated list of all maintenance records associated with a specific aircraft registration number. (US116)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Records retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Aircraft not found")
    })
    @GetMapping("/records/aircraft/{registrationNumber}")
    public ResponseEntity<PagedModel<EntityModel<ViewAllMaintenanceRecordsResponse>>> getAllMaintenanceRecordsForAircraft(
            @Parameter(description = "Unique registration number code of the aircraft (e.g. CS-TKA)") @PathVariable String registrationNumber,
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<ViewAllMaintenanceRecordsResponse> assembler) {

        RegistrationNumber regNum = new RegistrationNumber(registrationNumber);

        PaginatedResult<ViewAllMaintenanceRecordsResponse> result = viewAllMaintenanceRecordsUseCase.execute(
                regNum, pageable.getPageNumber(), pageable.getPageSize());
        Page<ViewAllMaintenanceRecordsResponse> page = new PageImpl<>(result.data(), pageable, result.totalElements());

        return ResponseEntity.ok(assembler.toModel(page, EntityModel::of));
    }

    /**
     * Endpoint to calculate and retrieve the total amount of maintenance hours performed across the entire fleet.
     * @return a ResponseEntity containing the total maintenance hours in the fleet
     */
    @Operation(summary = "Get total maintenance hours", description = "Calculates and returns the total amount of maintenance hours performed across the entire fleet. (US117)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total hours calculated and returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @GetMapping("/records/hours")
    public ResponseEntity<EntityModel<ViewTotalMaintenanceHoursInFleetResponse>> getTotalMaintenanceHoursInFleet() {
        ViewTotalMaintenanceHoursInFleetResponse response = viewTotalMaintenanceHoursInFleetUseCase.execute();
        EntityModel<ViewTotalMaintenanceHoursInFleetResponse> model = EntityModel.of(response,
                linkTo(methodOn(MaintenanceController.class).getTotalMaintenanceHoursInFleet()).withSelfRel());
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Delete a maintenance record", description = "Permanently removes a maintenance record by ID. Requires Maintenance Technician or Admin role.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Maintenance record deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Maintenance record not found")
    })
    @DeleteMapping("/records/{id}")
    public ResponseEntity<Void> deleteMaintenanceRecord(
            @Parameter(description = "Unique ID of the maintenance record") @PathVariable Long id) {
        deleteMaintenanceRecordUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete a maintenance template", description = "Permanently removes a maintenance template by ID. Requires Maintenance Technician or Admin role.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Maintenance template deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Maintenance template not found")
    })
    @DeleteMapping("/templates/{id}")
    public ResponseEntity<Void> deleteMaintenanceTemplate(
            @Parameter(description = "Unique ID of the maintenance template") @PathVariable Long id) {
        deleteMaintenanceTemplateUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete a maintenance part", description = "Permanently removes a maintenance part by ID. Requires Maintenance Technician or Admin role.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Maintenance part deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Maintenance part not found")
    })
    @DeleteMapping("/parts/{id}")
    public ResponseEntity<Void> deleteMaintenancePart(
            @Parameter(description = "Unique ID of the maintenance part") @PathVariable Long id) {
        deleteMaintenancePartUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Helper method to convert a MaintenanceRecordResponse into an EntityModel with HATEOAS links for further actions related to the maintenance record.
     * @param response the MaintenanceRecordResponse to be converted into an EntityModel
     * @return an EntityModel containing the MaintenanceRecordResponse and HATEOAS links for related actions
     */
    private EntityModel<MaintenanceRecordResponse> toHateoasModel(MaintenanceRecordResponse response) {
        EntityModel<MaintenanceRecordResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(MaintenanceController.class)
                .updateRecordStatusAndNotes(response.id(), null, null)).withRel("update-record"));
        return model;
    }
    /**
     * Helper method to parse HTTP ETags securely.
     * Extracts the numeric version from standard ETag formats like "5" or W/"5".
     */
    private Long parseEtagToVersion(String etag) {
        if (etag == null || etag.isBlank()) {
            throw new IllegalArgumentException("If-Match header cannot be empty.");
        }
        String cleanEtag = etag.replace("W/", "");
        cleanEtag = cleanEtag.replace("\"", "").trim();
        try {
            return Long.parseLong(cleanEtag);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid If-Match ETag format. Expected a numeric entity version.");
        }
    }
}