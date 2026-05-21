package aisafe.maintenance.infrastructure;

import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.maintenance.application.*;
import aisafe.maintenance.application.dtos.*;
import aisafe.maintenance.domain.MaintenancePart;
import aisafe.maintenance.domain.MaintenanceTemplate;
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
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/maintenance")
@Tag(name = "Maintenance", description = "Aircraft Maintenance Management — WP#2B")
public class MaintenanceController {

    private final CreateMaintenanceTemplateUseCase createMaintenanceTemplateUseCase;
    private final CreateMaintenanceRecordUseCase createMaintenanceRecordUseCase;
    private final CreateMaintenancePartUseCase createMaintenancePartUseCase;
    private final UpdateMaintenanceRecordUseCase updateMaintenanceRecordUseCase;
    private final ViewAllMaintenanceRecordsUseCase viewAllMaintenanceRecordsUseCase;
    private final ViewTotalMaintenanceHoursInFleetUseCase viewTotalMaintenanceHoursInFleetUseCase;

    public MaintenanceController(CreateMaintenanceTemplateUseCase createMaintenanceTemplateUseCase,
                                 CreateMaintenanceRecordUseCase createMaintenanceRecordUseCase,
                                 CreateMaintenancePartUseCase createMaintenancePartUseCase,
                                 UpdateMaintenanceRecordUseCase updateMaintenanceRecordUseCase,
                                 ViewAllMaintenanceRecordsUseCase viewAllMaintenanceRecordsUseCase,
                                 ViewTotalMaintenanceHoursInFleetUseCase viewTotalMaintenanceHoursInFleetUseCase) {
        this.createMaintenanceTemplateUseCase = createMaintenanceTemplateUseCase;
        this.createMaintenanceRecordUseCase = createMaintenanceRecordUseCase;
        this.createMaintenancePartUseCase = createMaintenancePartUseCase;
        this.updateMaintenanceRecordUseCase = updateMaintenanceRecordUseCase;
        this.viewAllMaintenanceRecordsUseCase = viewAllMaintenanceRecordsUseCase;
        this.viewTotalMaintenanceHoursInFleetUseCase = viewTotalMaintenanceHoursInFleetUseCase;
    }

    @Operation(summary = "Create maintenance template", description = "Registers a new maintenance template configuration in the system.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Template created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data supplied"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PostMapping("/templates")
    public ResponseEntity<MaintenanceTemplate> createMaintenanceTemplate(@Valid @RequestBody CreateMaintenanceTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createMaintenanceTemplateUseCase.execute(request));
    }

    @Operation(summary = "Register a maintenance part", description = "Adds a new hardware component or part to the maintenance catalog.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Part registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data supplied"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PostMapping("/parts")
    public ResponseEntity<MaintenancePart> createMaintenancePart(@Valid @RequestBody CreateMaintenancePartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createMaintenancePartUseCase.execute(request));
    }

    @Operation(summary = "Create a maintenance record", description = "Schedules or logs a new maintenance record for a specific aircraft.")
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

    @Operation(summary = "Update maintenance record", description = "Updates the operational status and notes of an existing maintenance record. Requires the 'If-Match' header specifying the current resource version to perform Optimistic Concurrency Locking check.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Record updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data or missing If-Match header"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Maintenance record not found"),
            @ApiResponse(responseCode = "409", description = "Conflict detected — The resource version has changed or matches a concurrency collision state")
    })
    @PatchMapping("/records/{id}")
    public ResponseEntity<EntityModel<MaintenanceRecordResponse>> updateRecordStatusAndNotes(
            @Parameter(description = "Unique ID of the maintenance record") @PathVariable Long id,
            @Parameter(description = "Current version entity state identifier for locking assessment") @RequestHeader(value = "If-Match") Long version,
            @Valid @RequestBody UpdateMaintenanceRecordsRequest request) {

        MaintenanceRecordResponse updatedRecord = updateMaintenanceRecordUseCase.execute(id, request, version);
        return ResponseEntity.ok(toHateoasModel(updatedRecord));
    }

    @Operation(summary = "Get maintenance records by aircraft", description = "Returns a paginated list of all maintenance records associated with a specific aircraft registration number.")
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

        Page<ViewAllMaintenanceRecordsResponse> recordsPage = viewAllMaintenanceRecordsUseCase.execute(regNum, pageable);

        PagedModel<EntityModel<ViewAllMaintenanceRecordsResponse>> pagedModel =
                assembler.toModel(recordsPage, record -> EntityModel.of(record));

        return ResponseEntity.ok(pagedModel);
    }

    @Operation(summary = "Get total maintenance hours", description = "Calculates and returns the total amount of maintenance hours performed across the entire fleet.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total hours calculated and returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @GetMapping("/records/hours")
    public ResponseEntity<ViewTotalMaintenanceHoursinFleetResponse> getTotalMaintenanceHoursInFleet() {
        return ResponseEntity.ok(viewTotalMaintenanceHoursInFleetUseCase.execute());
    }

    private EntityModel<MaintenanceRecordResponse> toHateoasModel(MaintenanceRecordResponse response) {
        EntityModel<MaintenanceRecordResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(MaintenanceController.class)
                .updateRecordStatusAndNotes(response.id(), response.version(), null)).withRel("update-record"));
        return model;
    }
}