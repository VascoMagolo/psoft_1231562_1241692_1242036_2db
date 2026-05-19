package aisafe.aircrafts.application.dtos;

import aisafe.aircrafts.domain.AircraftStatus;

/**
 * Request payload used to update an aircraft status.
 */
public record UpdateStatusRequest(AircraftStatus status) {
}
