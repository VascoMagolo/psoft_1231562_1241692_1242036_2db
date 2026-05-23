package aisafe.aircrafts.infrastructure;

import aisafe.aircrafts.application.ListAircraftModelsUseCase;
import aisafe.aircrafts.application.RegisterAircraftModelUseCase;
import aisafe.aircrafts.application.dtos.AircraftModelResponse;
import aisafe.aircrafts.application.dtos.ListAircraftModelsUseCaseResponse;
import aisafe.aircrafts.application.dtos.RegisterAircraftModelRequest;
import io.swagger.v3.oas.annotations.Operation;
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
 * Controller for managing aircraft models in the system. Provides endpoints for registering new models and listing existing ones.
 */
@RestController
@RequestMapping("/api/aircraftModels")
@Tag(name = "Aircraft Models", description = "Aircraft Model configurations and catalog — WP#1A")
public class AircraftModelController {

    private final RegisterAircraftModelUseCase registerAircraftModel;
    private final ListAircraftModelsUseCase listAircraftModels;

    public AircraftModelController(RegisterAircraftModelUseCase registerAircraftModel,
            ListAircraftModelsUseCase listAircraftModels) {
        this.registerAircraftModel = registerAircraftModel;
        this.listAircraftModels = listAircraftModels;
    }

    /**
     * Registers a new aircraft model in the system.
     * @param request the details of the aircraft model to register
     * @return a response entity containing the details of the newly registered aircraft model, along with HATEOAS links
     */
    @Operation(summary = "Register a new aircraft model", description = "Creates a new technical model specification for aircrafts in the catalog. Requires Fleet Manager role.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Aircraft model successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid request data supplied"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "409", description = "An aircraft model with the given name already exists")
    })
    @PostMapping
    public ResponseEntity<EntityModel<AircraftModelResponse>> createModel(
            @Valid @RequestBody RegisterAircraftModelRequest request) {
        AircraftModelResponse response = registerAircraftModel.execute(request);
        EntityModel<AircraftModelResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(AircraftModelController.class).getAllModels(Pageable.unpaged(), null))
                .withRel("all-models"));

        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    /**
     * Retrieves a paginated list of all aircraft models in the system.
     * @param pageable pagination and sorting information for the request
     * @param assembler assembler to convert the page of results into a HATEOAS-compliant paged model
     * @return a response entity containing a paginated list of aircraft models, along with HATEOAS links for navigation
     */
    @Operation(summary = "Get all aircraft models with pagination", description = "Returns a paginated catalog of all supported aircraft models in the system. Supports HATEOAS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated catalog returned successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<ListAircraftModelsUseCaseResponse>>> getAllModels(
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<ListAircraftModelsUseCaseResponse> assembler) {
        Page<ListAircraftModelsUseCaseResponse> page = listAircraftModels.execute(pageable);
        PagedModel<EntityModel<ListAircraftModelsUseCaseResponse>> pagedModel = assembler.toModel(page);

        return ResponseEntity.ok(pagedModel);
    }
}
