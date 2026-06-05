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

    public AircraftController(ViewAircraftDetailsUseCase viewAircraftDetails, ListAircraftUseCase listAircraft,
                              RegisterAircraftUseCase registerAircraft, SearchAircraftUseCase searchAircraft,
                              UpdateAircraftStatusUseCase updateAircraftStatus,
                              DeleteAircraftUseCase deleteAircraft) {
        this.viewAircraftDetails = viewAircraftDetails;
        this.listAircraft = listAircraft;
        this.registerAircraft = registerAircraft;
        this.searchAircraft = searchAircraft;
        this.updateAircraftStatus = updateAircraftStatus;
        this.deleteAircraft = deleteAircraft;
    }

    /**
     * Registers a new aircraft in the system with the provided details.
     * @param request the request containing the information to create a new aircraft profile
     * @return a response entity with the created aircraft details and appropriate HATEOAS links
     */
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

    /**
     * Retrieves a paginated list of all registered aircrafts in the system.
     * @param pageable pagination information for the request (page number, size, sorting)
     * @param assembler assembler to convert the Page of aircraft responses into a HATEOAS-compliant paged model with navigation links
     * @return a response entity containing the paginated list of aircrafts with HATEOAS links for navigation and details access.
     */
    @Operation(summary = "Get all aircrafts with pagination", description = "Returns a paginated list of all registered aircrafts. Supports HATEOAS navigation links.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list returned successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<ListAircraftsUseCaseResponse>>> getAllAircraft(
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<ListAircraftsUseCaseResponse> assembler) {

        Page<ListAircraftsUseCaseResponse> aircraftPage = listAircraft.execute(pageable);

        PagedModel<EntityModel<ListAircraftsUseCaseResponse>> pagedModel =
                assembler.toModel(aircraftPage, aircraft -> EntityModel.of(aircraft)
                        .add(linkTo(methodOn(AircraftController.class)
                                .getAircraftByRegistrationNumber(new RegistrationNumber(aircraft.registrationNumber())))
                                .withSelfRel()));

        return ResponseEntity.ok(pagedModel);
    }

    /**
     * Retrieves detailed information about a specific aircraft using its unique registration number.
     * @param registration the unique registration number of the aircraft to retrieve details for
     * @return a response entity containing the detailed information of the requested aircraft along with HATEOAS links for navigation and related actions
     */
    @Operation(summary = "Get aircraft details by registration number", description = "Returns complete technical and operational details for a specific aircraft using its unique registration identifier. (US103)")
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

    /**
     * Performs a search for aircrafts based on optional filtering criteria.
     * @param modelId the technical model identification number to filter by (optional)
     * @param status the current operational status of the aircraft to filter by (optional)
     * @param year the exact year the aircraft was manufactured to filter by (optional)
     * @param pageable pagination information for the request (page number, size, sorting)
     * @param assembler assembler to convert the Page of search results into a HATEOAS-compliant paged model with navigation links
     * @return a response entity containing the paginated list of aircrafts matching the search criteria with HATEOAS links for navigation and details access
     */
    @Operation(summary = "Search and filter aircrafts", description = "Advanced search that filters aircraft profiles dynamically by model ID, current status, or year of manufacturing with pagination support. (US104)")
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
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<SearchAircraftUseCaseResponse> assembler) {
        Page<SearchAircraftUseCaseResponse> results = searchAircraft.execute(modelId, status, year, pageable);

        PagedModel<EntityModel<SearchAircraftUseCaseResponse>> pagedModel =
                assembler.toModel(results, aircraft -> EntityModel.of(aircraft)
                        .add(linkTo(methodOn(AircraftController.class)
                                .getAircraftByRegistrationNumber(new RegistrationNumber(aircraft.registrationNumber())))
                                .withSelfRel()));

        return ResponseEntity.ok(pagedModel);
    }

    /**
     * Updates the operational status of an existing aircraft.
     * @param registration the unique registration number of the aircraft to update
     * @param ifMatchHeader the value of the 'If-Match' header containing the current version of the aircraft resource for optimistic locking validation
     * @param request the request containing the new status to set for the aircraft
     * @return a response entity containing the updated aircraft details along with HATEOAS links for navigation and related actions, or an appropriate error response if the update fails due to validation errors, version conflicts, or if the aircraft is not found
     */
    @Operation(summary = "Update aircraft operational status", description = "Updates the status of an existing aircraft. Requires the 'If-Match' header specifying the current resource version to perform Optimistic Concurrency Locking check. (US105)")
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
            @Parameter(description = "Current version entity state identifier for locking assessment")
            @RequestHeader(value = "If-Match", required = false) String ifMatchHeader,
            @Valid @RequestBody UpdateStatusRequest request) {

        Long version = parseEtagToVersion(ifMatchHeader);

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
    @DeleteMapping("/{registration}")
    public ResponseEntity<Void> deleteAircraft(
            @Parameter(description = "Unique registration number of the aircraft (e.g. CS-TKA)")
            @PathVariable RegistrationNumber registration) {
        deleteAircraft.execute(registration);
        return ResponseEntity.noContent().build();
    }

    /**
     * Helper method to convert a ViewAircraftDetailsResponse DTO into an EntityModel with HATEOAS links for navigation and related actions.
     * @param response the ViewAircraftDetailsResponse DTO to convert into an EntityModel
     * @param registration the registration number of the aircraft, used to create links to the aircraft details and related actions
     * @return an EntityModel containing the aircraft details and HATEOAS links for self, all aircrafts, and update status actions
     */
    private EntityModel<ViewAircraftDetailsResponse> toHateoasModel(ViewAircraftDetailsResponse response, RegistrationNumber registration) {
        EntityModel<ViewAircraftDetailsResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(AircraftController.class).getAircraftByRegistrationNumber(registration)).withSelfRel());
        model.add(linkTo(methodOn(AircraftController.class).getAllAircraft(Pageable.unpaged(), null)).withRel("all-aircrafts"));
        model.add(linkTo(methodOn(AircraftController.class).updateAircraftStatus(registration, null, null)).withRel("update-status"));
        return model;
    }

    /**
     * Helper method to parse HTTP ETags securely.
     * Extracts the numeric version from standard ETag formats like "5" or W/"5".
     */
    private Long parseEtagToVersion(String etag) {
        if (etag == null || etag.isBlank()) {
            throw new IllegalArgumentException("If-Match header is missing or empty. Please provide the current resource version.");
        }

        String cleanEtag = etag.replace("W/", "").replace("\"", "").trim();

        try {
            return Long.parseLong(cleanEtag);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid If-Match ETag format. Expected a numeric entity version.");
        }
    }
}