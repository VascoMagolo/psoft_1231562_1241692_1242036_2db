package aisafe.aircrafts.infrastructure;

import aisafe.aircrafts.application.*;
import aisafe.aircrafts.application.dtos.*;
import aisafe.aircrafts.domain.AircraftStatus;
import aisafe.aircrafts.domain.RegistrationNumber;
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
@RequestMapping("/api/aircrafts")
@Tag(name = "Aircrafts", description = "Aircraft management — WP#1A and WP#2B")
public class AircraftController {

    private final ViewAircraftDetailsUseCase viewAircraftDetails;
    private final ListAircraftUseCase listAircraft;
    private final RegisterAircraftUseCase registerAircraft;
    private final SearchAircraftUseCase searchAircraft;
    private final UpdateAircraftStatusUseCase updateAircraftStatus;
    private final PagedResourcesAssembler<ListAircraftsUseCaseResponse> listAssembler;
    private final PagedResourcesAssembler<SearchAircraftUseCaseResponse> searchAssembler;

    public AircraftController(ViewAircraftDetailsUseCase viewAircraftDetails, ListAircraftUseCase listAircraft,
                              RegisterAircraftUseCase registerAircraft, SearchAircraftUseCase searchAircraft,
                              UpdateAircraftStatusUseCase updateAircraftStatus,
                              PagedResourcesAssembler<ListAircraftsUseCaseResponse> listAssembler,
                              PagedResourcesAssembler<SearchAircraftUseCaseResponse> searchAssembler) {
        this.viewAircraftDetails = viewAircraftDetails;
        this.listAircraft = listAircraft;
        this.registerAircraft = registerAircraft;
        this.searchAircraft = searchAircraft;
        this.updateAircraftStatus = updateAircraftStatus;
        this.listAssembler = listAssembler;
        this.searchAssembler = searchAssembler;
    }

    @Operation(summary = "Register a new aircraft", description = "Creates a new aircraft profile configuration in the system. Requires Fleet Manager role.")
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

    @Operation(summary = "Get all aircrafts with pagination", description = "Returns a paginated list of all registered aircrafts. Supports HATEOAS navigation links.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list returned successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<ListAircraftsUseCaseResponse>>> getAllAircraft(
            @PageableDefault(size = 20) Pageable pageable) {

        Page<ListAircraftsUseCaseResponse> aircraftPage = listAircraft.execute(pageable);

        PagedModel<EntityModel<ListAircraftsUseCaseResponse>> pagedModel =
                listAssembler.toModel(aircraftPage, aircraft -> EntityModel.of(aircraft)
                        .add(linkTo(methodOn(AircraftController.class)
                                .getAircraftByRegistrationNumber(new RegistrationNumber(aircraft.registrationNumber())))
                                .withSelfRel()));

        return ResponseEntity.ok(pagedModel);
    }

    @Operation(summary = "Get aircraft details by registration number", description = "Returns complete technical and operational details for a specific aircraft using its unique registration identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aircraft details found and returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Aircraft not found with specified registration number")
    })
    @GetMapping("/{registration}")
    public ResponseEntity<EntityModel<ViewAircraftDetailsResponse>> getAircraftByRegistrationNumber(
            @Parameter(description = "Unique registration number code of the aircraft (e.g. CS-TKA)")
            @PathVariable RegistrationNumber registration) {

        ViewAircraftDetailsResponse aircraft = viewAircraftDetails.execute(registration);
        return ResponseEntity.ok(toHateoasModel(aircraft, registration));
    }

    @Operation(summary = "Search and filter aircrafts", description = "Advanced search that filters aircraft profiles dynamically by model ID, current status, or year of manufacturing with pagination support.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results returned successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @GetMapping("/search")
    public ResponseEntity<PagedModel<EntityModel<SearchAircraftUseCaseResponse>>> searchAircrafts(
            @Parameter(description = "Filter by technical model identification number") @RequestParam(required = false) Long modelId,
            @Parameter(description = "Filter by aircraft current operational status") @RequestParam(required = false) AircraftStatus status,
            @Parameter(description = "Filter by the exact year the aircraft was manufactured") @RequestParam(required = false) Integer year,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<SearchAircraftUseCaseResponse> results = searchAircraft.execute(modelId, status, year, pageable);

        PagedModel<EntityModel<SearchAircraftUseCaseResponse>> pagedModel =
                searchAssembler.toModel(results, aircraft -> EntityModel.of(aircraft)
                        .add(linkTo(methodOn(AircraftController.class)
                                .getAircraftByRegistrationNumber(new RegistrationNumber(aircraft.registrationNumber())))
                                .withSelfRel()));

        return ResponseEntity.ok(pagedModel);
    }

    @Operation(summary = "Update aircraft operational status", description = "Updates the status of an existing aircraft. Requires the 'If-Match' header specifying the current resource version to perform Optimistic Concurrency Locking check.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aircraft status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status configuration or missing required parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Aircraft profile not found"),
            @ApiResponse(responseCode = "409", description = "Conflict detected — The resource version has changed or matches a concurrency collision state")
    })
    @PatchMapping("/{registration}/status")
    public ResponseEntity<EntityModel<ViewAircraftDetailsResponse>> updateAircraftStatus(
            @Parameter(description = "Registration identification key of the target aircraft") @PathVariable RegistrationNumber registration,
            @Parameter(description = "Current version entity state identifier for locking assessment") @RequestHeader(value = "If-Match") Long version,
            @Valid @RequestBody UpdateStatusRequest request) {

        ViewAircraftDetailsResponse updatedAircraft = updateAircraftStatus.execute(registration, String.valueOf(request.status()), version);
        return ResponseEntity.ok(toHateoasModel(updatedAircraft, registration));
    }

    private EntityModel<ViewAircraftDetailsResponse> toHateoasModel(ViewAircraftDetailsResponse response, RegistrationNumber registration) {
        EntityModel<ViewAircraftDetailsResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(AircraftController.class).getAircraftByRegistrationNumber(registration)).withSelfRel());
        model.add(linkTo(methodOn(AircraftController.class).getAllAircraft(Pageable.unpaged())).withRel("all-aircrafts"));
        model.add(linkTo(methodOn(AircraftController.class).updateAircraftStatus(registration, response.version(), null)).withRel("update-status"));
        return model;
    }
}