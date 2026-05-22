package aisafe.routes.infrastructure;

import aisafe.routes.application.*;
import aisafe.routes.application.dtos.CreateRouteRequest;
import aisafe.routes.application.dtos.UpdateRouteRequest;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteHistory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final CreateRouteUseCase createRoute;
    private final ViewRouteHistoryUseCase viewRouteHistory;
    private final UpdateRouteUseCase updateRoute;
    private final DesactivateRouteUseCase desactivateRoute;
    private final ViewRouteDetailsUseCase viewRouteDetails;
    private final ListRoutesFromAirportUseCase listRoutesFromAirport;
    private final SearchRoutesUseCase searchRoutes;

    public RouteController(CreateRouteUseCase createRoute,
                           ViewRouteHistoryUseCase viewRouteHistory,
                           UpdateRouteUseCase updateRoute,
                           DesactivateRouteUseCase desactivateRoute,
                           ViewRouteDetailsUseCase viewRouteDetails,
                           ListRoutesFromAirportUseCase listRoutesFromAirport,
                           SearchRoutesUseCase searchRoutes) {
        this.createRoute = createRoute;
        this.viewRouteHistory = viewRouteHistory;
        this.updateRoute = updateRoute;
        this.desactivateRoute = desactivateRoute;
        this.viewRouteDetails = viewRouteDetails;
        this.listRoutesFromAirport = listRoutesFromAirport;
        this.searchRoutes = searchRoutes;
    }

    // US110: Create a flight route
    @PostMapping
    public ResponseEntity<Route> createRoute(@RequestBody CreateRouteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createRoute.execute(request));
    }

    // US111: Keep track of route history
    @GetMapping("/{id}/history")
    public ResponseEntity<List<RouteHistory>> getRouteHistory(@PathVariable Long id) {
        return ResponseEntity.ok(viewRouteHistory.execute(id));
    }

    // US112: Update route details
    @PutMapping("/{id}")
    public ResponseEntity<Route> updateRoute(@PathVariable Long id, @RequestBody UpdateRouteRequest request) {
        return ResponseEntity.ok(updateRoute.execute(id, request));
    }

    // US112: Deactivate a route
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Route> deactivateRoute(@PathVariable Long id) {
        return ResponseEntity.ok(desactivateRoute.execute(id));
    }

    // US113: View route details by ID
    @GetMapping("/{id}")
    public ResponseEntity<Route> getRouteDetails(@PathVariable Long id) {
        return ResponseEntity.ok(viewRouteDetails.execute(id));
    }

    // US113: View routes from a specific airport
    @GetMapping("/airport/{iataCode}")
    public ResponseEntity<List<Route>> getRoutesFromAirport(@PathVariable String iataCode) {
        return ResponseEntity.ok(listRoutesFromAirport.execute(iataCode.toUpperCase()));
    }

    // US114: Search for routes (by origin and/or destination)
    @GetMapping("/search")
    public ResponseEntity<List<Route>> searchRoutes(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination) {
        return ResponseEntity.ok(searchRoutes.execute(origin, destination));
    }
}
