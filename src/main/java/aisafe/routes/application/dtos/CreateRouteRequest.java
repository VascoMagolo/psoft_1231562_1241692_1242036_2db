package aisafe.routes.application.dtos;

public record CreateRouteRequest(
        String originIataCode,
        String destinationIataCode,
        Integer estimatedFlightTime,
        Double minimumRange,
        Integer minimumCapacity
) {}
