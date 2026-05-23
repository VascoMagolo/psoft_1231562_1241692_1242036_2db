package aisafe.routes.application.dtos;

/**
 * Data transfer object representing the information required to create a new route.
 */
public record CreateRouteRequest(
        String originIataCode,
        String destinationIataCode,
        Integer estimatedFlightTime,
        Double minimumRange,
        Integer minimumCapacity
) {}
