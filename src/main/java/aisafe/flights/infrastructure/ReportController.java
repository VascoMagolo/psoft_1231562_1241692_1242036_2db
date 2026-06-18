package aisafe.flights.infrastructure;

import aisafe.flights.application.GenerateFlightUtilizationReportUseCase;
import aisafe.flights.domain.RouteUtilizationData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Endpoints for generating reports")
public class ReportController {

    private final GenerateFlightUtilizationReportUseCase generateFlightUtilizationReport;

    public ReportController(GenerateFlightUtilizationReportUseCase generateFlightUtilizationReport) {
        this.generateFlightUtilizationReport = generateFlightUtilizationReport;
    }

    @Operation(summary = "Generate flight utilization report", description = "Aggregates completed flight data to provide insights into route popularity. (US229)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date range")
    })
    @GetMapping("/flight-utilization")
    public ResponseEntity<List<RouteUtilizationData>> getFlightUtilization(
            @Parameter(description = "Start date (inclusive)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @Parameter(description = "End date (inclusive)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") Integer size) {

        List<RouteUtilizationData> report = generateFlightUtilizationReport.execute(startDate, endDate, page, size);
        return ResponseEntity.ok(report);
    }
}
