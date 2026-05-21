package aisafe.routes.application.dtos;

public record RouteResponse(
        Long id,
        String originIataCode,
        String destinationIataCode,
        Integer estimatedFlightTime,
        Double minimumRange,
        Integer minimumCapacity,
        boolean active
) {}
