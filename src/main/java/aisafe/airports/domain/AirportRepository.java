package aisafe.airports.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AirportRepository extends JpaRepository<Airport, Long> {
    Optional<Airport> findByIataCodeCode(String code);

    boolean existsByIataCodeCode(String code);

    @Query("SELECT a FROM Airport a WHERE " +
           "(:name IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:city IS NULL OR LOWER(a.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:country IS NULL OR LOWER(a.country) LIKE LOWER(CONCAT('%', :country, '%')))")
    List<Airport> searchAirports(@Param("name") String name,
                                 @Param("city") String city,
                                 @Param("country") String country);

    List<Airport> findAllByCountry(String country);

    List<Airport> findAllByRegion(String region);
}
