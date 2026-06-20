package aisafe.airports.domain;

import aisafe.aircrafts.domain.ModelName;
import aisafe.shared.domain.BaseRepository;

import java.util.List;

public interface AircraftCertificationRepository extends BaseRepository<AircraftCertification> {
    List<AircraftCertification> findByAirportCode(IataCode airportCode);
    boolean existsByAirportCodeAndAircraftModelName(IataCode airportCode, ModelName aircraftModelName);
}
