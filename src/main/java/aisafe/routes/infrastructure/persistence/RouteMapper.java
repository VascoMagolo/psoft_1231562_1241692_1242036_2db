package aisafe.routes.infrastructure.persistence;

import aisafe.routes.domain.Route;

public class RouteMapper {

    public static Route toDomain(RouteJpaEntity entity) {
        Route route = new Route(
                entity.getOriginCode(),
                entity.getDestinationCode(),
                entity.getEstimatedFlightTime(),
                entity.getMinimumRange(),
                entity.getMinimumCapacity()
        );
        route.setId(entity.getId());
        route.setVersion(entity.getVersion());
        route.setActive(entity.isActive());
        return route;
    }

    public static RouteJpaEntity toJpa(Route route) {
        return new RouteJpaEntity(
                route.getOrigin().getCode(),
                route.getDestination().getCode(),
                route.getEstimatedFlightTime(),
                route.getMinimumRange(),
                route.getMinimumCapacity(),
                route.isActive()
        );
    }
}
