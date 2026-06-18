package aisafe.flights.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface SpringDataScheduledFlightRepository extends JpaRepository<ScheduledFlightJpaEntity, Long> {

    @Query("SELECT f.aircraft.model.modelName AS modelName, COUNT(f.id) AS utilizationValue " +
           "FROM ScheduledFlightJpaEntity f " +
           "WHERE f.status = 'COMPLETED' " +
           "GROUP BY f.aircraft.model.modelName " +
           "ORDER BY utilizationValue DESC")
    List<ModelUtilizationProjection> findTopModelsByAssignments(Pageable pageable);

    @Query("SELECT f.aircraft.model.modelName AS modelName, " +
           "SUM(TIMESTAMPDIFF(SECOND, f.departureDateTime, f.arrivalDateTime)) / 3600 AS utilizationValue " +
           "FROM ScheduledFlightJpaEntity f " +
           "WHERE f.status = 'COMPLETED' " +
           "GROUP BY f.aircraft.model.modelName " +
           "ORDER BY utilizationValue DESC")
    List<ModelUtilizationProjection> findTopModelsByFlightHours(Pageable pageable);

    @Query("SELECT COALESCE(SUM(TIMESTAMPDIFF(SECOND, f.departureDateTime, f.arrivalDateTime)), 0) / 3600.0 " +
           "FROM ScheduledFlightJpaEntity f " +
           "WHERE f.status = 'COMPLETED' AND f.aircraft.registrationNumber.number = :registration")
    Double calculateTotalOperationalHoursByRegistration(@Param("registration") String registration);

    @Query("SELECT f FROM ScheduledFlightJpaEntity f " +
           "WHERE f.aircraft.registrationNumber.number = :registration " +
           "AND f.status = 'COMPLETED' " +
           "AND f.departureDateTime >= :startDate " +
           "AND f.departureDateTime <= :endDate")
    List<ScheduledFlightJpaEntity> findFlightsForUtilization(@Param("registration") String registration, @Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT f FROM ScheduledFlightJpaEntity f " +
           "WHERE f.aircraft.registrationNumber.number = :registration " +
           "ORDER BY f.departureDateTime ASC")
    List<ScheduledFlightJpaEntity> findByAircraftRegistration(@Param("registration") String registration);

    @Query("SELECT COUNT(f) > 0 FROM ScheduledFlightJpaEntity f " +
           "WHERE f.aircraft.registrationNumber.number = :registration " +
           "AND f.departureDateTime < :arrivalDateTime " +
           "AND f.arrivalDateTime > :departureDateTime")
    boolean hasOverlappingFlights(@Param("registration") String registration,
                                  @Param("departureDateTime") OffsetDateTime departureDateTime,
                                  @Param("arrivalDateTime") OffsetDateTime arrivalDateTime);

    @Query("SELECT COUNT(f) FROM ScheduledFlightJpaEntity f " +
           "WHERE f.route.originCode = :originCode " +
           "AND f.route.destinationCode = :destinationCode")
    long countByRoute(@Param("originCode") String originCode, @Param("destinationCode") String destinationCode);

    boolean existsByAircraftRegistrationNumberNumber(String registration);

    @Query("SELECT f.route.id AS routeId, f.route.originCode AS originCode, f.route.destinationCode AS destinationCode, COUNT(f.id) AS flightCount " +
           "FROM ScheduledFlightJpaEntity f " +
           "WHERE f.status = 'COMPLETED' " +
           "AND (cast(:startDate as timestamp) IS NULL OR f.departureDateTime >= :startDate) " +
           "AND (cast(:endDate as timestamp) IS NULL OR f.arrivalDateTime <= :endDate) " +
           "GROUP BY f.route.id, f.route.originCode, f.route.destinationCode " +
           "ORDER BY flightCount DESC")
    List<RouteUtilizationProjection> findFlightUtilizationReports(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate, Pageable pageable);
}
