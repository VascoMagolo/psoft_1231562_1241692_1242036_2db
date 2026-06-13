package aisafe.routes.infrastructure.persistence;

import aisafe.routes.domain.RouteHistory;

public class RouteHistoryMapper {

    public static RouteHistory toDomain(RouteHistoryJpaEntity entity) {
        RouteHistory history = new RouteHistory(
                entity.getRoute().getOriginCode(),
                entity.getRoute().getDestinationCode(),
                entity.getChangeDescription(),
                entity.getChangedBy()
        );
        history.setChangedAt(entity.getChangedAt());
        return history;
    }

    public static RouteHistoryJpaEntity toJpa(RouteHistory history, RouteJpaEntity route) {
        return new RouteHistoryJpaEntity(
                route,
                history.getChangeDescription(),
                history.getChangedAt(),
                history.getChangedBy()
        );
    }
}
