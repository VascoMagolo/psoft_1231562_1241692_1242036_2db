package aisafe.routes.application.dtos;

/**
 * Data transfer object representing the information to update an existing route.
 */
public record UpdateRouteRequest(
        Integer estimatedFlightTime,
        Double minimumRange,
        Integer minimumCapacity,
        Boolean active
) {}
