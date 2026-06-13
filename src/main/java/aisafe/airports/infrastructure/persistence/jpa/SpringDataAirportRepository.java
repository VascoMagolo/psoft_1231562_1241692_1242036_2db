package aisafe.airports.infrastructure.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataAirportRepository extends JpaRepository<AirportJpaEntity, Long> {

    Optional<AirportJpaEntity> findByIataCode(String iataCode);

    boolean existsByIataCode(String iataCode);

    @Query("SELECT a FROM AirportJpaEntity a WHERE " +
           "(:name IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:city IS NULL OR LOWER(a.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:country IS NULL OR LOWER(a.country) LIKE LOWER(CONCAT('%', :country, '%')))")
    Page<AirportJpaEntity> searchAirports(@Param("name") String name,
                                          @Param("city") String city,
                                          @Param("country") String country,
                                          Pageable pageable);
}
