package aisafe.routes.application.dtos;

/**
 * Data transfer object representing the details of a route.
 */
public record RouteResponse(
        Long id,
        String originIataCode,
        String destinationIataCode,
        Integer estimatedFlightTime,
        Double minimumRange,
        Integer minimumCapacity,
        boolean active
) {}
