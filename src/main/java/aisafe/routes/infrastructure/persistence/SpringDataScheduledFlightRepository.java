package aisafe.routes.infrastructure.persistence;

import aisafe.aircrafts.infrastructure.persistence.TopUtilizedModelProjection;
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
    List<TopUtilizedModelProjection> findTopModelsByAssignments(Pageable pageable);

    @Query(value = "SELECT am.model_name AS modelName, " +
                   "SUM(DATEDIFF(SECOND, f.departure_date_time, f.arrival_date_time)) / 3600 AS utilizationValue " +
                   "FROM scheduled_flight f " +
                   "JOIN aircrafts a ON f.aircraft_id = a.id " +
                   "JOIN aircraft_models am ON a.model_id = am.id " +
                   "WHERE f.status = 'COMPLETED' " +
                   "GROUP BY am.model_name " +
                   "ORDER BY utilizationValue DESC", nativeQuery = true)
    List<TopUtilizedModelProjection> findTopModelsByFlightHours(Pageable pageable);

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
}
