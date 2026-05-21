package aisafe.routes.dtos;

public record CreateRouteRequest(
        String originIataCode,
        String destinationIataCode,
        Integer estimatedFlightTime,
        Double minimumRange,
        Integer minimumCapacity
) {}