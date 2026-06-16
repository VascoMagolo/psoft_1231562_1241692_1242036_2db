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

    @Query(value = "SELECT am.model_name AS modelName, " +
                   "SUM(DATEDIFF(SECOND, f.departure_date_time, f.arrival_date_time)) / 3600 AS utilizationValue " +
                   "FROM scheduled_flight f " +
                   "JOIN aircrafts a ON f.aircraft_id = a.id " +
                   "JOIN aircraft_models am ON a.model_id = am.id " +
                   "WHERE f.status = 'COMPLETED' " +
                   "GROUP BY am.model_name " +
                   "ORDER BY utilizationValue DESC", nativeQuery = true)
    List<ModelUtilizationProjection> findTopModelsByFlightHours(Pageable pageable);

    @Query(value = "SELECT COALESCE(SUM(DATEDIFF(SECOND, f.departure_date_time, f.arrival_date_time)), 0) / 3600.0 " +       
                   "FROM scheduled_flight f " +
                   "JOIN aircrafts a ON f.aircraft_id = a.id " +
                   "WHERE f.status = 'COMPLETED' AND a.registration_number = :registration", nativeQuery = true)
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
}
