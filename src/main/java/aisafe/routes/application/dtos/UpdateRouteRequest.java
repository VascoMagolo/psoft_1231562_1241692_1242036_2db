package aisafe.routes.application.dtos;

public record UpdateRouteRequest(
        Integer estimatedFlightTime,
        Double minimumRange,
        Integer minimumCapacity,
        Boolean active
) {}
