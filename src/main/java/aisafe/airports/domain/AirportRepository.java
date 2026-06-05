package aisafe.airports.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Airport entities.
 */
@Repository
public interface AirportRepository extends JpaRepository<Airport, Long> {
    Optional<Airport> findByIataCodeCode(String code);

    boolean existsByIataCodeCode(String code);

    /**
     * Searches for airports based on the provided criteria.
     * @param name the name of the airport to search for
     * @param city the city where the airport is located
     * @param country the country where the airport is located
     * @param pageable pagination information for the search results
     * @return a page of Airport entities matching the search criteria
     */
    @Query("SELECT a FROM Airport a WHERE " +
           "(:name IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:city IS NULL OR LOWER(a.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:country IS NULL OR LOWER(a.country) LIKE LOWER(CONCAT('%', :country, '%')))")
    Page<Airport> searchAirports(@Param("name") String name,
                                 @Param("city") String city,
                                 @Param("country") String country,
                                 Pageable pageable);

    List<Airport> findAllByCountry(String country);

    List<Airport> findAllByRegion(String region);

}
