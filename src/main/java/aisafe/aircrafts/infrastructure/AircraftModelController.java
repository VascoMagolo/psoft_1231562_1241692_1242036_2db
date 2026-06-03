package aisafe.aircrafts.infrastructure;

import aisafe.aircrafts.application.DeleteAircraftModelUseCase;
import aisafe.aircrafts.application.ListAircraftModelsUseCase;
import aisafe.aircrafts.application.RegisterAircraftModelUseCase;
import aisafe.aircrafts.application.dtos.AircraftModelResponse;
import aisafe.aircrafts.application.dtos.ListAircraftModelsUseCaseResponse;
import aisafe.aircrafts.application.dtos.RegisterAircraftModelRequest;
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

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/aircraftModels")
@Tag(name = "Aircraft Models", description = "Aircraft Model configurations and catalog - WP#1A")
public class AircraftModelController {

    private final RegisterAircraftModelUseCase registerAircraftModel;
    private final ListAircraftModelsUseCase listAircraftModels;
    private final DeleteAircraftModelUseCase deleteAircraftModel;

    public AircraftModelController(RegisterAircraftModelUseCase registerAircraftModel,
                                   ListAircraftModelsUseCase listAircraftModels,
                                   DeleteAircraftModelUseCase deleteAircraftModel) {
        this.registerAircraftModel = registerAircraftModel;
        this.listAircraftModels = listAircraftModels;
        this.deleteAircraftModel = deleteAircraftModel;
    }

    @Operation(summary = "Register a new aircraft model")
    @PostMapping
    public ResponseEntity<EntityModel<AircraftModelResponse>> createModel(
            @Valid @RequestBody RegisterAircraftModelRequest request) {

        AircraftModelResponse response = registerAircraftModel.execute(request);
        EntityModel<AircraftModelResponse> model = EntityModel.of(response);

        model.add(linkTo(methodOn(AircraftModelController.class).getAllAircraftModels(Pageable.unpaged(), null))
                .withRel("all-models"));

        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @Operation(summary = "Get all aircraft models with pagination")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<ListAircraftModelsUseCaseResponse>>> getAllAircraftModels(
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<ListAircraftModelsUseCaseResponse> assembler) {

        List<ListAircraftModelsUseCaseResponse> modelsList = listAircraftModels.execute(
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        Page<ListAircraftModelsUseCaseResponse> modelsPage = new PageImpl<>(modelsList, pageable, modelsList.size());

        PagedModel<EntityModel<ListAircraftModelsUseCaseResponse>> pagedModel =
                assembler.toModel(modelsPage, model -> EntityModel.of(model)
                        .add(linkTo(methodOn(AircraftModelController.class)
                                .getAircraftModelByName(model.modelName()))
                                .withSelfRel()));

        return ResponseEntity.ok(pagedModel);
    }

    @Operation(summary = "Delete an aircraft model", description = "Permanently removes an aircraft model by name. Requires Backoffice Operator or Admin role.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Aircraft model deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Aircraft model not found")
    })
    @DeleteMapping("/{modelName}")
    public ResponseEntity<Void> deleteModel(
            @Parameter(description = "Unique Name of the aircraft model") @PathVariable String modelName) {

        deleteAircraftModel.execute(modelName);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get aircraft model details by name")
    @GetMapping("/{modelName}")
    public ResponseEntity<EntityModel<AircraftModelResponse>> getAircraftModelByName(
            @PathVariable String modelName) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}