package aisafe.routes.infrastructure.persistence.jpa;

import aisafe.routes.domain.Route;

public class RouteMapper {

    private RouteMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Route toDomain(RouteJpaEntity entity) {
        Route route = new Route(
                entity.getOriginCode().getCode(),
                entity.getDestinationCode().getCode(),
                entity.getEstimatedFlightTime(),
                entity.getMinimumRange(),
                entity.getMinimumCapacity()
        );
        route.changeStatus(entity.getStatus());
        return route;
    }

    public static RouteJpaEntity toJpa(Route route) {
        return new RouteJpaEntity(
                route.getOrigin().getCode(),
                route.getDestination().getCode(),
                route.getEstimatedFlightTime(),
                route.getMinimumRange(),
                route.getMinimumCapacity(),
                route.getStatus()
        );
    }
}
