package aisafe.airports.domain;

import aisafe.shared.domain.BaseRepository;
import aisafe.shared.domain.PaginatedResult;

import java.util.Optional;

/**
 * Repository interface for managing Airport entities.
 */
public interface AirportRepository extends BaseRepository<Airport> {
    Optional<Airport> findByIataCodeCode(String code);
    boolean existsByIataCodeCode(String code);
    PaginatedResult<Airport> searchAirports(String name, String city, String country, int pageNumber, int pageSize);
}
