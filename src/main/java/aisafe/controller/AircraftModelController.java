package aisafe.controller;

import aisafe.model.entities.AircraftModel;
import aisafe.services.AircraftModelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/aircraft-models")
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
}
