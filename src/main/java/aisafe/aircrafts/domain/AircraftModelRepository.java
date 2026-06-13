package aisafe.aircrafts.domain;

import aisafe.shared.domain.BaseRepository;

import java.util.List;
import java.util.Optional;

/**
 * Aircraft Model Repository
 */
public interface AircraftModelRepository extends BaseRepository<AircraftModel> {
    Optional<AircraftModel> findByModelName(String modelName);
    boolean existsByModelName(String modelName);
    List<AircraftModel> findAll(int pageNumber, int pageSize);
}
