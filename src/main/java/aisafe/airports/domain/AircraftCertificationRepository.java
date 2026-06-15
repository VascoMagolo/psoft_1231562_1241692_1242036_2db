package aisafe.airports.domain;

import aisafe.shared.domain.BaseRepository;

import java.util.List;

/**
 * Repository interface for managing AircraftCertification entities.
 */
public interface AircraftCertificationRepository extends BaseRepository<AircraftCertification> {
    List<AircraftCertification> findByAirport(Airport airport);
    boolean existsByAirportAndAircraftModelName(Airport airport, String aircraftModelName);
}
