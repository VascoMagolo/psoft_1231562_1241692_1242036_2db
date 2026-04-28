package aisafe.aircrafts.infrastructure;

import aisafe.aircrafts.application.ListAircraftModelsUseCase;
import aisafe.aircrafts.application.RegisterAircraftModelUseCase;
import aisafe.aircrafts.domain.AircraftModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aircraftModels")
public class AircraftModelController {
    private final RegisterAircraftModelUseCase RegisterAircraftModel;
    private final ListAircraftModelsUseCase ListAircraftModels;
    public AircraftModelController(RegisterAircraftModelUseCase registerAircraftModel, ListAircraftModelsUseCase listAircraftModels) {
        this.RegisterAircraftModel = registerAircraftModel;
        this.ListAircraftModels = listAircraftModels;
    }

    @PostMapping
    public ResponseEntity<AircraftModel> createModel(@RequestBody AircraftModel model) {
        AircraftModel savedModel = RegisterAircraftModel.execute(model);
        return new ResponseEntity<>(savedModel, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AircraftModel>> getAllModels() {
        List<AircraftModel> models = ListAircraftModels.execute();
        return ResponseEntity.ok(models);
    }
}
