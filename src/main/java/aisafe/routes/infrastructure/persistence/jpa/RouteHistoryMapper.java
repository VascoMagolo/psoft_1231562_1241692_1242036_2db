package aisafe.routes.infrastructure.persistence.jpa;

import aisafe.airports.domain.IataCode;
import aisafe.routes.domain.RouteHistory;

public class RouteHistoryMapper {

    private RouteHistoryMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static RouteHistory toDomain(RouteHistoryJpaEntity entity) {
        return new RouteHistory(
                new IataCode(entity.getRoute().getOriginCode().getCode()),
                new IataCode(entity.getRoute().getDestinationCode().getCode()),
                entity.getChangeDescription(),
                entity.getChangedAt(),
                entity.getChangedBy()
        );
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
