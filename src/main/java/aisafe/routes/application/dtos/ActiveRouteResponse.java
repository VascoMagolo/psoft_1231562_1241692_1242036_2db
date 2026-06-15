package aisafe.routes.application.dtos;

import aisafe.routes.domain.RouteStatus;

public record ActiveRouteResponse(
        String originIataCode,
        String destinationIataCode,
        Integer estimatedFlightTime,
        Double minimumRange,
        Integer minimumCapacity,
        RouteStatus status,
        Double distanceKm,
        Long popularity
) {}
