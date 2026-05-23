package aisafe.airports.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for adding an aircraft certification to an airport.
 * @param aircraftModelId ID of the aircraft model to certify at this airport
 */
@Schema(description = "Request body for adding an aircraft certification to an airport")
public record AddCertificationRequest(
        @Schema(description = "ID of the aircraft model to certify at this airport", example = "1")
        @NotNull(message = "Aircraft model ID is required") Long aircraftModelId
) {}
