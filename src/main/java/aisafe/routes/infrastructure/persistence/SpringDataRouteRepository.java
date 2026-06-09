package aisafe.routes.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataRouteRepository extends JpaRepository<RouteJpaEntity, Long> {
    Page<RouteJpaEntity> findByOriginCode(String originCode, Pageable pageable);
    Page<RouteJpaEntity> findByDestinationCode(String destinationCode, Pageable pageable);
    Page<RouteJpaEntity> findByOriginCodeAndDestinationCode(String originCode, String destinationCode, Pageable pageable);
    boolean existsByOriginCodeAndDestinationCode(String originCode, String destinationCode);
    List<RouteJpaEntity> findByOriginCodeOrDestinationCode(String originCode, String destinationCode);

    @Query("SELECT r FROM RouteJpaEntity r WHERE r.active = true AND r.minimumRange <= :range AND r.minimumCapacity <= :capacity")
    List<RouteJpaEntity> findCompatibleRoutes(@Param("range") Double range, @Param("capacity") Integer capacity);
}
