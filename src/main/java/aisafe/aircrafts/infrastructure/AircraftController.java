package aisafe.aircrafts.infrastructure;

import aisafe.aircrafts.application.*;
import aisafe.aircrafts.application.dtos.*;
import aisafe.aircrafts.domain.AircraftStatus;
import aisafe.aircrafts.domain.RegistrationNumber;
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

    @PostMapping
    public ResponseEntity<EntityModel<ViewAircraftDetailsResponse>> registerAircraft(
            @Valid @RequestBody RegisterAircraftRequest request) {

        ViewAircraftDetailsResponse createdAircraft = registerAircraft.execute(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toHateoasModel(createdAircraft, new RegistrationNumber(request.registrationNumber())));
    }

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

    @GetMapping("/{registration}")
    public ResponseEntity<EntityModel<ViewAircraftDetailsResponse>> getAircraftByRegistrationNumber(
            @PathVariable RegistrationNumber registration) {

        ViewAircraftDetailsResponse aircraft = viewAircraftDetails.execute(registration);
        return ResponseEntity.ok(toHateoasModel(aircraft, registration));
    }

    @GetMapping("/search")
    public ResponseEntity<PagedModel<EntityModel<SearchAircraftUseCaseResponse>>> searchAircrafts(
            @RequestParam(required = false) Long modelId,
            @RequestParam(required = false) AircraftStatus status,
            @RequestParam(required = false) Integer year,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<SearchAircraftUseCaseResponse> results = searchAircraft.execute(modelId, status, year, pageable);

        PagedModel<EntityModel<SearchAircraftUseCaseResponse>> pagedModel =
                searchAssembler.toModel(results, aircraft -> EntityModel.of(aircraft)
                        .add(linkTo(methodOn(AircraftController.class)
                                .getAircraftByRegistrationNumber(new RegistrationNumber(aircraft.registrationNumber())))
                                .withSelfRel()));

        return ResponseEntity.ok(pagedModel);
    }

    @PatchMapping("/{registration}/status")
    public ResponseEntity<EntityModel<ViewAircraftDetailsResponse>> updateAircraftStatus(
            @PathVariable RegistrationNumber registration,
            @RequestHeader(value = "If-Match") Long version,
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