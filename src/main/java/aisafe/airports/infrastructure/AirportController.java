package aisafe.airports.infrastructure;

import aisafe.airports.application.*;
import aisafe.airports.application.dtos.*;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirplaneCertification;
import aisafe.model.entities.Route;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/airports")
public class AirportController {
    private final RegisterAirportUseCase registerAirport;
    private final AddAirportCertificationUseCase addCertification;
    private final ViewAirportDetailsUseCase viewAirportDetails;
    private final SearchAirportUseCase searchAirport;
    private final UpdateAirportStatusUseCase updateAirportStatus;
    private final UpdateAirportDetailsUseCase updateAirportDetails;
    private final ViewAirportRoutesUseCase viewAirportRoutes;
    private final AirportStatisticsUseCase airportStatistics;
    private final ListAirportsByRegionUseCase listAirportsByRegion;

    public AirportController(RegisterAirportUseCase registerAirport,
                             AddAirportCertificationUseCase addCertification,
                             ViewAirportDetailsUseCase viewAirportDetails,
                             SearchAirportUseCase searchAirport,
                             UpdateAirportStatusUseCase updateAirportStatus,
                             UpdateAirportDetailsUseCase updateAirportDetails,
                             ViewAirportRoutesUseCase viewAirportRoutes,
                             AirportStatisticsUseCase airportStatistics,
                             ListAirportsByRegionUseCase listAirportsByRegion) {
        this.registerAirport = registerAirport;
        this.addCertification = addCertification;
        this.viewAirportDetails = viewAirportDetails;
        this.searchAirport = searchAirport;
        this.updateAirportStatus = updateAirportStatus;
        this.updateAirportDetails = updateAirportDetails;
        this.viewAirportRoutes = viewAirportRoutes;
        this.airportStatistics = airportStatistics;
        this.listAirportsByRegion = listAirportsByRegion;
    }

    // US106 + US207: Register airport (basic fields + optional facilities/photo)
    @PostMapping
    public ResponseEntity<Airport> registerAirport(@RequestBody RegisterAirportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registerAirport.execute(request));
    }

    // US106a: Add airplane certification to airport
    @PostMapping("/{iataCode}/certifications")
    public ResponseEntity<AirplaneCertification> addCertification(
            @PathVariable String iataCode,
            @RequestBody AddCertificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addCertification.execute(iataCode.toUpperCase(), request));
    }

    // US107: View airport details by IATA code
    @GetMapping("/{iataCode}")
    public ResponseEntity<Airport> getAirport(@PathVariable String iataCode) {
        return ResponseEntity.ok(viewAirportDetails.execute(iataCode.toUpperCase()));
    }

    // US108: Search airports by city, country, or name
    @GetMapping("/search")
    public ResponseEntity<List<Airport>> searchAirports(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country) {
        return ResponseEntity.ok(searchAirport.execute(name, city, country));
    }

    // US109: Update airport operational status
    @PatchMapping("/{iataCode}/status")
    public ResponseEntity<Airport> updateStatus(
            @PathVariable String iataCode,
            @RequestBody UpdateAirportStatusRequest request) {
        return ResponseEntity.ok(updateAirportStatus.execute(iataCode.toUpperCase(), request.status()));
    }

    // US208: Update airport operational hours and contact information
    @PatchMapping("/{iataCode}/details")
    public ResponseEntity<Airport> updateDetails(
            @PathVariable String iataCode,
            @RequestBody UpdateAirportDetailsRequest request) {
        return ResponseEntity.ok(updateAirportDetails.execute(iataCode.toUpperCase(), request));
    }

    // US209: View all routes departing from or arriving at a specific airport
    @GetMapping("/{iataCode}/routes")
    public ResponseEntity<List<Route>> getRoutes(@PathVariable String iataCode) {
        return ResponseEntity.ok(viewAirportRoutes.execute(iataCode.toUpperCase()));
    }

    // US210: Statistics — busiest airports by number of routes
    @GetMapping("/statistics/busiest")
    public ResponseEntity<List<AirportStatisticsResponse>> getBusiestAirports() {
        return ResponseEntity.ok(airportStatistics.execute());
    }

    // US211: List airports grouped by region (default) or country (?by=country)
    @GetMapping("/grouped")
    public ResponseEntity<List<AirportGroupResponse>> getAirportsGrouped(
            @RequestParam(defaultValue = "region") String by) {
        return ResponseEntity.ok(listAirportsByRegion.execute(by));
    }
}
