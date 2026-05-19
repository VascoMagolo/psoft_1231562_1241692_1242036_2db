package aisafe.aircrafts.infrastructure;

import aisafe.aircrafts.application.*;
import aisafe.aircrafts.application.dtos.ListAircraftsUseCaseResponse;
import aisafe.aircrafts.application.dtos.RegisterAircraftRequest;
import aisafe.aircrafts.application.dtos.SearchAircraftUseCaseResponse;
import aisafe.aircrafts.application.dtos.UpdateStatusRequest;
import aisafe.aircrafts.application.dtos.ViewAircraftDetailsResponse;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftStatus;
import aisafe.aircrafts.domain.RegistrationNumber;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller that exposes aircraft registration, listing, lookup, search, and status update endpoints.
 * {@link #getAircraftByRegistrationNumber(RegistrationNumber)} - GET /api/aircrafts/{registration} - Look up an aircraft by registration number and return detailed information.
 * {@link #getAllAircraft()} - GET /api/aircrafts - List all registered aircraft with summary information.
 * {@link #registerAircraft(RegisterAircraftRequest)} - POST /api/aircrafts/register - Register a new aircraft with the provided details.
 * {@link #searchAircrafts(Long, AircraftStatus, Integer)} - GET /api/aircrafts/search - Search for aircraft based on optional criteria like model, status, and manufacturing year.
 * {@link #updateAircraftStatus(RegistrationNumber, UpdateStatusRequest)} - PATCH /api/aircrafts/{registration}/status - Update the status of an existing aircraft.
 */
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
    public ResponseEntity<Aircraft> registerAircraft(
            @RequestBody RegisterAircraftRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registerAircraft.execute(request));

    }

    @GetMapping
    public ResponseEntity<List<ListAircraftsUseCaseResponse>> getAllAircraft() {
        List<ListAircraftsUseCaseResponse> aircraft = listAircraft.execute();
        return ResponseEntity.ok(aircraft);
    }

    @GetMapping("/{registration}")
    public ResponseEntity<ViewAircraftDetailsResponse> getAircraftByRegistrationNumber(
            @PathVariable RegistrationNumber registration){
        ViewAircraftDetailsResponse aircraft = viewAircraftDetails.execute(registration);
        return ResponseEntity.ok(aircraft);
    }

    @GetMapping("/search")
    public ResponseEntity<List<SearchAircraftUseCaseResponse>> searchAircrafts(
            @RequestParam(required = false) Long modelId,
            @RequestParam(required = false) AircraftStatus status,
            @RequestParam(required = false) Integer year) {

        List<SearchAircraftUseCaseResponse> results = searchAircraft.execute(modelId, status, year);
        return ResponseEntity.ok(results);
    }

    @PatchMapping("/{registration}/status")
    public ResponseEntity<Aircraft> updateAircraftStatus(
            @PathVariable RegistrationNumber registration,
            @RequestBody UpdateStatusRequest request) {
        Aircraft updatedAircraft = updateAircraftStatus.execute(registration, String.valueOf(request.status()));

        return ResponseEntity.ok(updatedAircraft);
    }
}
