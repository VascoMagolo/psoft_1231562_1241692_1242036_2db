package aisafe.aircrafts.domain;

import aisafe.shared.application.dtos.PaginatedResult;

import java.util.Optional;

/**
    * Repository interface for managing `Aircraft` entities.
 */
public interface AircraftRepository {
    long count();
    Optional<Aircraft> findByRegistrationNumber(RegistrationNumber registrationNumber);
    boolean existsByRegistrationNumber(RegistrationNumber registrationNumber);
    PaginatedResult<Aircraft> findAll(int pageNumber, int pageSize);
    PaginatedResult<Aircraft> searchAircrafts(String modelName, AircraftStatus status, Integer year, int pageNumber, int pageSize);
    void save(Aircraft aircraft, Long clientVersion);
    void delete(Aircraft aircraft);
}