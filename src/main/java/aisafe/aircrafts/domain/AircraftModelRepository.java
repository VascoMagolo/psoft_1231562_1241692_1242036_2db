package aisafe.aircrafts.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Aircraft Model Repository
 */
@Repository
public interface AircraftModelRepository {
    long count();
    Optional<AircraftModel> findByModelName(String modelName);
    boolean existsByModelName(String modelName);
    List<AircraftModel> findAll(int pageNumber, int pageSize);
    void save(AircraftModel model);
    void delete(AircraftModel model);
}
