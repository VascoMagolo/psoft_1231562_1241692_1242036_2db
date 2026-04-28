package aisafe.aircrafts.infrastructure;

import aisafe.aircrafts.application.*;
import aisafe.aircrafts.application.dtos.UpdateStatusRequest;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.model.enums.AircraftStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aircrafts")
public class AircraftController {
    private final ViewAircraftDetailsUseCase viewAircraftDetails;
    private final ListAircraftUseCase listAircraft;
    private final RegisterAircraftUseCase registerAircraft;
    private final SearchAircraftUseCase searchAircraft;
    private final UpdateAircraftStatusUseCase updateAircraftStatus;

    public AircraftController(ViewAircraftDetailsUseCase viewAircraftDetails, ListAircraftUseCase listAircraft, RegisterAircraftUseCase registerAircraft, SearchAircraftUseCase searchAircraft, UpdateAircraftStatusUseCase updateAircraftStatus) {
        this.viewAircraftDetails = viewAircraftDetails;
        this.listAircraft = listAircraft;
        this.registerAircraft = registerAircraft;
        this.searchAircraft = searchAircraft;
        this.updateAircraftStatus = updateAircraftStatus;
    }

    @PostMapping("/register")
    public ResponseEntity<Aircraft> registerAircraft(@RequestBody Aircraft aircraft) {
        Aircraft savedAircraft = registerAircraft.execute(aircraft);
        return new ResponseEntity<>(savedAircraft, HttpStatus.CREATED);

    }

    @GetMapping
    public ResponseEntity<List<Aircraft>> getAllAircraft() {
        List<Aircraft> aircraft = listAircraft.execute();
        return ResponseEntity.ok(aircraft);
    }

    @GetMapping("/{registration}")
    public ResponseEntity<Aircraft> getAircraftByRegistrationNumber(@PathVariable String registration){
        Aircraft aircraft = viewAircraftDetails.execute(registration);
        return ResponseEntity.ok(aircraft);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Aircraft>> searchAircrafts(
            @RequestParam(required = false) Long modelId,
            @RequestParam(required = false) AircraftStatus status,
            @RequestParam(required = false) Integer year) {

        List<Aircraft> results = searchAircraft.execute(modelId, status, year);
        return ResponseEntity.ok(results);
    }

    @PatchMapping("/{registration}/status")
    public ResponseEntity<Aircraft> updateAircraftStatus(
            @PathVariable String registration,
            @RequestBody UpdateStatusRequest request) {
        Aircraft updatedAircraft = updateAircraftStatus.execute(registration, request.status());

        return ResponseEntity.ok(updatedAircraft);
    }
}
