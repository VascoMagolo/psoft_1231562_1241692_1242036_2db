package aisafe.routes.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RouteHistoryRepository extends JpaRepository<RouteHistory, Long> {
    List<RouteHistory> findAllByRouteId(Long routeId);
}