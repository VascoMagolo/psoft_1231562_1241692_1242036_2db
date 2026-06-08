package aisafe.routes.application.dtos;

public record ActiveRouteResponse(
        Long id,
        String originIataCode,
        String destinationIataCode,
        Integer estimatedFlightTime,
        Double minimumRange,
        Integer minimumCapacity,
        boolean active,
        Double distanceKm,
        Long popularity
) {}
