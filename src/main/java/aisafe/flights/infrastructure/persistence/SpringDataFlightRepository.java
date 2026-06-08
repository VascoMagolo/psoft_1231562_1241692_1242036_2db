package aisafe.flights.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface SpringDataFlightRepository extends JpaRepository<FlightJpaEntity, Long> {

    List<FlightJpaEntity> findByAircraftRegistrationNumberOrderByDepartureDateTimeAsc(String aircraftRegistrationNumber);

    long countByRouteId(Long routeId);

    @Query("""
            SELECT COUNT(f) > 0
            FROM FlightJpaEntity f
            WHERE f.aircraftRegistrationNumber = :aircraftRegistrationNumber
              AND f.departureDateTime < :arrivalDateTime
              AND f.arrivalDateTime > :departureDateTime
            """)
    boolean hasOverlappingFlights(
            @Param("aircraftRegistrationNumber") String aircraftRegistrationNumber,
            @Param("departureDateTime") OffsetDateTime departureDateTime,
            @Param("arrivalDateTime") OffsetDateTime arrivalDateTime);
}
