package aisafe.routes.domain;

import java.util.List;

public interface RouteHistoryRepository {
    void save(RouteHistory history);
    List<RouteHistory> findAllByRouteId(Long routeId);
}
