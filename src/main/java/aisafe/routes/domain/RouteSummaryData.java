package aisafe.routes.domain;

import aisafe.airports.domain.IataCode;

public record RouteSummaryData(
        IataCode origin,
        IataCode destination,
        Integer estimatedFlightTime,
        Double minimumRange,
        Integer minimumCapacity,
        RouteStatus status,
        Long version
) {}
