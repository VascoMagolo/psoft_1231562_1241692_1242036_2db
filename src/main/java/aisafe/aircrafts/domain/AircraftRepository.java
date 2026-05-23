package aisafe.aircrafts.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

/**
 * Spring Data repository for `Aircraft` entities. It supports lookups by registration number and filtered search by model, status, and manufacturing year.
 */
@Repository
public interface AircraftRepository extends JpaRepository<Aircraft,Long> {
    Optional<Aircraft> findByRegistrationNumber(RegistrationNumber registrationNumber);
    boolean existsByRegistrationNumber(RegistrationNumber registrationNumber);

    /**
     * Searches for aircrafts based on optional criteria: model ID, status, and manufacturing year.
     * The query dynamically applies filters only if the corresponding parameters are provided (non-null).
     * Results are paginated according to the `Pageable` parameter.
     * @param modelId the ID of the aircraft model to filter by (optional)
     * @param status the status of the aircraft to filter by (optional)
     * @param year the manufacturing year to filter by (optional)
     * @param pageable pagination and sorting information
     * @return a page of aircrafts matching the search criteria
     */
    @Query("SELECT a FROM Aircraft a WHERE " +
            "(:modelId IS NULL OR a.model.id = :modelId) AND " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:year IS NULL OR YEAR(a.manufacturingDate) = :year)")
    Page<Aircraft> searchAircrafts(@Param("modelId") Long modelId,
                                   @Param("status") AircraftStatus status,
                                   @Param("year") Integer year,
                                   Pageable pageable);
}
