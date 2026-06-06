package aisafe.routes.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataRouteHistoryRepository extends JpaRepository<RouteHistoryJpaEntity, Long> {
    List<RouteHistoryJpaEntity> findAllByRouteId(Long routeId);
}
