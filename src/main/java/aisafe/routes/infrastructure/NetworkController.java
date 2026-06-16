package aisafe.routes.infrastructure;

import aisafe.routes.application.CalculateTotalNetworkDistanceUseCase;
import aisafe.routes.application.dtos.TotalDistanceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/network")
@Tag(name = "Network", description = "Flight Network Calculations - WP#3B")
public class NetworkController {

    private final CalculateTotalNetworkDistanceUseCase calculateTotalNetworkDistance;

    public NetworkController(CalculateTotalNetworkDistanceUseCase calculateTotalNetworkDistance) {
        this.calculateTotalNetworkDistance = calculateTotalNetworkDistance;
    }

    @Operation(summary = "Calculate total network distance", description = "Calculates the total distance covered by active routes. (US215)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total distance calculated successfully")
    })
    @GetMapping("/total-distance")
    public ResponseEntity<TotalDistanceResponse> getTotalDistance() {
        return ResponseEntity.ok(calculateTotalNetworkDistance.execute());
    }
}
