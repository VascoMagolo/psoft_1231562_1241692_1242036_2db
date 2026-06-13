package aisafe.routes.application.dtos;

import aisafe.routes.domain.RouteStatus;

/**
 * Data transfer object representing the details of a route.
 */
public record RouteResponse(
        String originIataCode,
        String destinationIataCode,
        Integer estimatedFlightTime,
        Double minimumRange,
        Integer minimumCapacity,
        RouteStatus status,
        Long version
) {}
