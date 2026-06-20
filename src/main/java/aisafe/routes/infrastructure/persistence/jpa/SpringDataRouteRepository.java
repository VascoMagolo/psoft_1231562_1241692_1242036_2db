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
    Optional<RouteJpaEntity> findByOriginCode_CodeAndDestinationCode_Code(String originCode, String destinationCode);
    Page<RouteJpaEntity> findByOriginCode_Code(String originCode, Pageable pageable);
    Page<RouteJpaEntity> findByDestinationCode_Code(String destinationCode, Pageable pageable);
    Page<RouteJpaEntity> findByOriginCode_CodeAndDestinationCode_Code(String originCode, String destinationCode, Pageable pageable);
    boolean existsByOriginCode_CodeAndDestinationCode_Code(String originCode, String destinationCode);
    List<RouteJpaEntity> findByOriginCode_CodeOrDestinationCode_Code(String originCode, String destinationCode);
    List<RouteJpaEntity> findByStatus(RouteStatus status);

    @Query("SELECT r FROM RouteJpaEntity r WHERE r.status = aisafe.routes.domain.RouteStatus.ACTIVE AND r.minimumRange <= :range AND r.minimumCapacity <= :capacity")
    List<RouteJpaEntity> findCompatibleRoutes(@Param("range") Double range, @Param("capacity") Integer capacity);

    @Query("SELECT r.originCode.code AS originCode, r.destinationCode.code AS destinationCode, " +
           "r.estimatedFlightTime AS estimatedFlightTime, r.minimumRange AS minimumRange, " +
           "r.minimumCapacity AS minimumCapacity, r.status AS status, r.version AS version " +
           "FROM RouteJpaEntity r WHERE r.originCode.code = :code OR r.destinationCode.code = :code")
    List<RouteSummaryRow> findSummariesByAirportCode(@Param("code") String code);

    interface RouteSummaryRow {
        String getOriginCode();
        String getDestinationCode();
        Integer getEstimatedFlightTime();
        Double getMinimumRange();
        Integer getMinimumCapacity();
        RouteStatus getStatus();
        Long getVersion();
    }
}
