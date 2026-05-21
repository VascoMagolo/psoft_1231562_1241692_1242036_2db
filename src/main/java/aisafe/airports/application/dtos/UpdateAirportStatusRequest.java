package aisafe.airports.application.dtos;

import aisafe.airports.domain.AirportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for updating airport operational status")
public record UpdateAirportStatusRequest(
        @Schema(description = "New status for the airport", example = "CLOSED", allowableValues = {"OPERATIONAL", "CLOSED", "UNDER_MAINTENANCE"})
        @NotNull(message = "Status is required") AirportStatus status
) {}
