package aisafe.airports.domain;

import aisafe.shared.domain.PaginatedResult;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Airport entities.
 */
public interface AirportRepository {
    long count();
    Optional<Airport> findByIataCodeCode(String code);
    boolean existsByIataCodeCode(String code);
    List<Airport> findAll();
    PaginatedResult<Airport> searchAirports(String name, String city, String country, int pageNumber, int pageSize);
    void save(Airport airport);
    void delete(Airport airport);
}
