package aisafe.aircrafts.application.dtos;

import aisafe.aircrafts.domain.AircraftStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload used to update an aircraft status.
 */
public record UpdateStatusRequest(
        @NotNull(message = "New status cannot be null")AircraftStatus status) {
}
