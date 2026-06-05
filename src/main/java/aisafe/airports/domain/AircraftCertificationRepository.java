package aisafe.airports.domain;

import java.util.List;

/**
 * Repository interface for managing AircraftCertification entities.
 */
public interface AircraftCertificationRepository {
    List<AircraftCertification> findByAirport(Airport airport);
    boolean existsByAirportAndAircraftModelName(Airport airport, String aircraftModelName);
    void save(AircraftCertification certification);
}
