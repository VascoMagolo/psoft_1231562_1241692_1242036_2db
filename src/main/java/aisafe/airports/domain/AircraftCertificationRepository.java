package aisafe.airports.domain;

import aisafe.aircrafts.domain.ModelName;
import aisafe.shared.domain.BaseRepository;

import java.util.List;

public interface AircraftCertificationRepository extends BaseRepository<AircraftCertification> {
    List<AircraftCertification> findByAirport(Airport airport);
    boolean existsByAirportAndAircraftModelName(Airport airport, ModelName aircraftModelName);
}
