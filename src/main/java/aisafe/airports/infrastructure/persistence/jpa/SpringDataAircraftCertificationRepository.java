package aisafe.airports.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataAircraftCertificationRepository extends JpaRepository<AircraftCertificationJpaEntity, Long> {

    List<AircraftCertificationJpaEntity> findByAirport(AirportJpaEntity airport);

    boolean existsByAirportAndAircraftModelName(AirportJpaEntity airport, String aircraftModelName);
}
