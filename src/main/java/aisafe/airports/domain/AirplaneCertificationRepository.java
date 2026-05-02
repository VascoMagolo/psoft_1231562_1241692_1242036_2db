package aisafe.airports.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AirplaneCertificationRepository extends JpaRepository<AirplaneCertification, Long> {
    List<AirplaneCertification> findByAirport(Airport airport);

    boolean existsByAirportAndAircraftModelId(Airport airport, Long aircraftModelId);
}
