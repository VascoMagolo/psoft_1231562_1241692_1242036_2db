package aisafe.routes.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataRouteHistoryRepository extends JpaRepository<RouteHistoryJpaEntity, Long> {
    @Query("SELECT h FROM RouteHistoryJpaEntity h WHERE h.route.originCode = :originCode AND h.route.destinationCode = :destinationCode")
    List<RouteHistoryJpaEntity> findAllByRoute(@Param("originCode") String originCode, @Param("destinationCode") String destinationCode);
}
