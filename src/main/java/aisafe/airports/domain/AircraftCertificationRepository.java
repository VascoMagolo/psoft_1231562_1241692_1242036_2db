package aisafe.airports.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository for `AircraftCertification` entities.
 */
@Repository
public interface AircraftCertificationRepository extends JpaRepository<AircraftCertification, Long> {

    List<AircraftCertification> findByAirport(Airport airport);

    boolean existsByAirportAndAircraftModelName(Airport airport, String aircraftModelName);
}