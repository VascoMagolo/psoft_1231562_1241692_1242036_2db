package aisafe.maintenance.domain;

import aisafe.shared.domain.PaginatedResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MaintenanceRecordRepository {
    long count();
    boolean existsByStartDateAndPartAndTemplate(LocalDateTime startDate, MaintenancePart part, MaintenanceTemplate template);
    boolean existsByPart(MaintenancePart part);
    boolean existsByTemplate(MaintenanceTemplate template);
    boolean existsByAircraftRegistration(String aircraftRegistration);
    PaginatedResult<MaintenanceRecord> findByAircraftRegistration(String aircraftRegistration, int pageNumber, int pageSize);
    List<MaintenanceRecord> findAll();
    Optional<MaintenanceRecord> findByRecordId(UUID recordId);
    void save(MaintenanceRecord record);
    void delete(MaintenanceRecord record);
}
