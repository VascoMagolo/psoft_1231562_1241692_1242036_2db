package aisafe.routes.infrastructure.readmodel;

import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteStatus;

/**
 * A route as kept by the read model and as sent in the route events: only what the listings need
 * (no history).
 */
public record RouteView(
        String origin,
        String destination,
        Integer estimatedFlightTime,
        Double minimumRange,
        Integer minimumCapacity,
        RouteStatus status
) {
    public static RouteView from(Route route) {
        return new RouteView(route.getOrigin().getCode(), route.getDestination().getCode(),
                route.getEstimatedFlightTime(), route.getMinimumRange(), route.getMinimumCapacity(), route.getStatus());
    }

    public Route toRoute() {
        Route route = new Route(origin, destination, estimatedFlightTime, minimumRange, minimumCapacity);
        route.changeStatus(status);
        return route;
    }
}
