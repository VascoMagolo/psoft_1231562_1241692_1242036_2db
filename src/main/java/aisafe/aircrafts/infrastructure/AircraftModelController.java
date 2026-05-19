package aisafe.aircrafts.infrastructure;

import aisafe.aircrafts.application.ListAircraftModelsUseCase;
import aisafe.aircrafts.application.RegisterAircraftModelUseCase;
import aisafe.aircrafts.application.dtos.RegisterAircraftModelRequest;
import aisafe.aircrafts.application.dtos.ListAircraftModelsUseCaseResponse;
import aisafe.aircrafts.domain.AircraftModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller that exposes aircraft model creation and listing endpoints.
 * {@link #createModel(RegisterAircraftModelRequest)} - POST /api/aircraftModels - Register a new aircraft model with the provided details.
 * {@link #getAllModels()} - GET /api/aircraftModels - List all registered aircraft models.
 */
@RestController
@RequestMapping("/api/aircraftModels")
public class AircraftModelController {
    private final RegisterAircraftModelUseCase registerAircraftModel;
    private final ListAircraftModelsUseCase listAircraftModels;
    public AircraftModelController(RegisterAircraftModelUseCase registerAircraftModel, ListAircraftModelsUseCase listAircraftModels) {
        this.registerAircraftModel = registerAircraftModel;
        this.listAircraftModels = listAircraftModels;
    }

    @PostMapping
    public ResponseEntity<AircraftModel> createModel(
            @RequestBody RegisterAircraftModelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registerAircraftModel.execute(request));
    }

    @GetMapping
    public ResponseEntity<List<ListAircraftModelsUseCaseResponse>> getAllModels() {
        List<ListAircraftModelsUseCaseResponse> models = listAircraftModels.execute();
        return ResponseEntity.ok(models);
    }
}
