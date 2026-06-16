package aisafe.routes.infrastructure.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import aisafe.routes.domain.RouteStatus;

public interface SpringDataRouteRepository extends JpaRepository<RouteJpaEntity, Long> {
    Optional<RouteJpaEntity> findByOriginCodeAndDestinationCode(String originCode, String destinationCode);
    Page<RouteJpaEntity> findByOriginCode(String originCode, Pageable pageable);
    Page<RouteJpaEntity> findByDestinationCode(String destinationCode, Pageable pageable);
    Page<RouteJpaEntity> findByOriginCodeAndDestinationCode(String originCode, String destinationCode, Pageable pageable);
    boolean existsByOriginCodeAndDestinationCode(String originCode, String destinationCode);
    List<RouteJpaEntity> findByOriginCodeOrDestinationCode(String originCode, String destinationCode);
    List<RouteJpaEntity> findByStatus(RouteStatus status);

    @Query("SELECT r FROM RouteJpaEntity r WHERE r.status = aisafe.routes.domain.RouteStatus.ACTIVE AND r.minimumRange <= :range AND r.minimumCapacity <= :capacity")
    List<RouteJpaEntity> findCompatibleRoutes(@Param("range") Double range, @Param("capacity") Integer capacity);

    @Query("SELECT r.originCode AS originCode, r.destinationCode AS destinationCode, " +
           "r.estimatedFlightTime AS estimatedFlightTime, r.minimumRange AS minimumRange, " +
           "r.minimumCapacity AS minimumCapacity, r.status AS status, r.version AS version " +
           "FROM RouteJpaEntity r WHERE r.originCode = :code OR r.destinationCode = :code")
    List<RouteSummaryRow> findSummariesByAirportCode(@Param("code") String code);

    interface RouteSummaryRow {
        String getOriginCode();
        String getDestinationCode();
        Integer getEstimatedFlightTime();
        Double getMinimumRange();
        Integer getMinimumCapacity();
        aisafe.routes.domain.RouteStatus getStatus();
        Long getVersion();
    }
}
