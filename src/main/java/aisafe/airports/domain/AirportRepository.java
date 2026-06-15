package aisafe.airports.domain;

import aisafe.shared.domain.BaseRepository;
import aisafe.shared.domain.PaginatedResult;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Airport entities.
 */
public interface AirportRepository extends BaseRepository<Airport> {
    Optional<Airport> findByIataCode(IataCode code);
    boolean existsByIataCode(IataCode code);
    Long findVersionFor(IataCode code);
    PaginatedResult<Airport> searchAirports(String name, String city, String country, int pageNumber, int pageSize);
    List<AirportStatisticsData> findStatistics();
    List<AirportGroupingData> findAllGroupingByRegion();
    List<AirportGroupingData> findAllGroupingByCountry();
}
