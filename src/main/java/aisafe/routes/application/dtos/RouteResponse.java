package aisafe.routes.application.dtos;

import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteStatus;

/**
 * Data transfer object representing the details of a route.
 */
public record RouteResponse(
        String originIataCode,
        String destinationIataCode,
        Integer estimatedFlightTime,
        Double minimumRange,
        Integer minimumCapacity,
        RouteStatus status,
        Long version
) {
    public static RouteResponse from(Route route) {
        return new RouteResponse(
                route.getOrigin().getCode(),
                route.getDestination().getCode(),
                route.getEstimatedFlightTime(),
                route.getMinimumRange(),
                route.getMinimumCapacity(),
                route.getStatus(),
                null
        );
    }

    public static RouteResponse from(Route route, Long version) {
        return new RouteResponse(
                route.getOrigin().getCode(),
                route.getDestination().getCode(),
                route.getEstimatedFlightTime(),
                route.getMinimumRange(),
                route.getMinimumCapacity(),
                route.getStatus(),
                version
        );
    }
}
