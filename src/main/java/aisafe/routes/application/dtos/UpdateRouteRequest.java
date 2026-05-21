package aisafe.routes.dtos;

public record UpdateRouteRequest(
        Integer estimatedFlightTime,
        Double minimumRange,
        Integer minimumCapacity,
        Boolean active
) {}