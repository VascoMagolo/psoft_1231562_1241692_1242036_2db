package aisafe.aircrafts.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data repository for `AircraftModel` entities.
 */
@Repository
public interface AircraftModelRepository extends JpaRepository<AircraftModel, Long> {
    Optional<AircraftModel> findByModelName(String modelName);
    boolean existsByModelName(String modelName);
}
