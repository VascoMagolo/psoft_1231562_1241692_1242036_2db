package aisafe.aircrafts.domain;

import aisafe.shared.domain.BaseRepository;
import aisafe.shared.domain.PaginatedResult;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing `Aircraft` entities.
 */
public interface AircraftRepository extends BaseRepository<Aircraft> {
    Optional<Aircraft> findByRegistrationNumber(RegistrationNumber registrationNumber);
    boolean existsByRegistrationNumber(RegistrationNumber registrationNumber);
    PaginatedResult<Aircraft> findAll(int pageNumber, int pageSize);
    PaginatedResult<Aircraft> searchAircrafts(String modelName, AircraftStatus status, Integer year, String feature, int pageNumber, int pageSize);
    boolean anyAircraftExistsForModel(String modelName);
    Long findVersionFor(RegistrationNumber reg);
}