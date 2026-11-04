package aisafe.routes.infrastructure.readmodel;

import aisafe.routes.application.RouteDistanceService;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteStatus;

/**
 * A route as kept by the read model and as sent in the route events: what the listings and
 * flight-ops need (no history).
 */
public record RouteView(
        String origin,
        String destination,
        Integer estimatedFlightTime,
        Double minimumRange,
        Integer minimumCapacity,
        RouteStatus status,
        Double distanceKm
) {
    /**
     * With the distance between its airports, which flight-ops checks against the aircraft's range
     * when scheduling (it was in the answer of C2). Null if an airport is not on this replica yet:
     * the view is still kept, and flight-ops then skips that check, as it does for a route without it.
     */
    public static RouteView from(Route route, RouteDistanceService distances) {
        Double distance;
        try {
            distance = distances.calculateDistanceKm(route);
        } catch (RuntimeException e) {
            distance = null;
        }
        return new RouteView(route.getOrigin().getCode(), route.getDestination().getCode(),
                route.getEstimatedFlightTime(), route.getMinimumRange(), route.getMinimumCapacity(), route.getStatus(), distance);
    }

    public Route toRoute() {
        Route route = new Route(origin, destination, estimatedFlightTime, minimumRange, minimumCapacity);
        route.changeStatus(status);
        return route;
    }
}
