package aisafe.aircrafts.application.dtos;

import aisafe.shared.domain.PaginatedResult;

/**
 * Top-level response for the fleet status overview endpoint.
 * Contains the total aircraft count and a list of status groups, each wrapping its aircraft in a PaginatedResult.
 */
public record FleetStatusResponse(
        long totalAircraft,
        PaginatedResult<FleetStatusGroupResponse> statusGroups) {
}
