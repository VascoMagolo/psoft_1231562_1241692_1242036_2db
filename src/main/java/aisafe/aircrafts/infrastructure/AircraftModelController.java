package aisafe.aircrafts.infrastructure;

import aisafe.aircrafts.domain.AircraftModel;
import aisafe.services.AircraftModelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aircraftModels")
public class AircraftModelController {
    private final AircraftModelService service;

    public AircraftModelController(AircraftModelService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AircraftModel> createModel(@RequestBody AircraftModel model) {
        AircraftModel savedModel = service.registerAircraftModel(model);

        return new ResponseEntity<>(savedModel, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AircraftModel>> getAllModels() {
        List<AircraftModel> models = service.findAll();
        return ResponseEntity.ok(models);
    }
}
