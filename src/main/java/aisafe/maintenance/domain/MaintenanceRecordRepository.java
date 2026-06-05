package aisafe.maintenance.domain;

import aisafe.shared.domain.PaginatedResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MaintenanceRecordRepository {
    long count();
    boolean existsByStartDateAndPartAndTemplate(LocalDateTime startDate, MaintenancePart part, MaintenanceTemplate template);
    PaginatedResult<MaintenanceRecord> findByAircraftRegistration(String aircraftRegistration, int pageNumber, int pageSize);
    List<MaintenanceRecord> findAll();
    Optional<MaintenanceRecord> findById(Long id);
    void save(MaintenanceRecord record);
    void delete(MaintenanceRecord record);
}
