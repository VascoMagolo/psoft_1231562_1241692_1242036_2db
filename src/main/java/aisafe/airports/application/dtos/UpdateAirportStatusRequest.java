package aisafe.airports.application.dtos;

import aisafe.airports.domain.AirportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating the operational status of an airport.
 * @param status the new operational status to set for the airport
 */
@Schema(description = "Request body for updating airport operational status")
public record UpdateAirportStatusRequest(
        @Schema(description = "New status for the airport", example = "CLOSED", allowableValues = {"OPERATIONAL", "CLOSED", "UNDER_MAINTENANCE"})
        @NotNull(message = "Status is required") AirportStatus status
) {}
