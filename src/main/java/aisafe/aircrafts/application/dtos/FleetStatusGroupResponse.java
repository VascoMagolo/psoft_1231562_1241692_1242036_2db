package aisafe.aircrafts.application.dtos;

import aisafe.aircrafts.domain.AircraftStatus;
import aisafe.shared.domain.PaginatedResult;

/**
 * Represents a group of aircraft sharing the same operational status.
 */
public record FleetStatusGroupResponse(
        AircraftStatus status,
        PaginatedResult<FleetStatusAircraftResponse> aircrafts) {
}
