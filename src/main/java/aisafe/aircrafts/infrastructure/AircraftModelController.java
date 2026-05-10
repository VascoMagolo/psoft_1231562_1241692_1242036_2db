package aisafe.aircrafts.infrastructure;

import aisafe.aircrafts.application.ListAircraftModelsUseCase;
import aisafe.aircrafts.application.RegisterAircraftModelUseCase;
import aisafe.aircrafts.application.dtos.RegisterAircraftModelRequest;
import aisafe.aircrafts.domain.AircraftModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.List;

@RestController
@RequestMapping("/api/aircraftModels")
public class AircraftModelController {
    private final RegisterAircraftModelUseCase registerAircraftModel;
    private final ListAircraftModelsUseCase ListAircraftModels;
    public AircraftModelController(RegisterAircraftModelUseCase registerAircraftModel, ListAircraftModelsUseCase listAircraftModels) {
        this.registerAircraftModel = registerAircraftModel;
        this.ListAircraftModels = listAircraftModels;
    }

    @PostMapping
    public ResponseEntity<AircraftModel> createModel(
            @RequestBody RegisterAircraftModelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registerAircraftModel.execute(request));
//        AircraftModel newModel = registerAircraftModel.execute(request);
//        EntityModel<AircraftModel> resource = EntityModel.of(newModel);
//        resource.add(linkTo(methodOn(AircraftModelController.class).getAllModels()).withRel("all-models"));
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    @GetMapping
    public ResponseEntity<List<AircraftModel>> getAllModels() {
        List<AircraftModel> models = ListAircraftModels.execute();
        return ResponseEntity.ok(models);
    }
}
