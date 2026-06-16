package aisafe.maintenance.domain;

import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.shared.domain.BaseRepository;
import aisafe.shared.domain.PaginatedResult;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface MaintenanceRecordRepository extends BaseRepository<MaintenanceRecord> {
    boolean existsByStartDateAndTemplate(LocalDateTime startDate, MaintenanceTemplate template);
    boolean existsByPartsContaining(MaintenancePart part);
    boolean existsByTemplate(MaintenanceTemplate template);
    boolean existsByAircraftRegistration(RegistrationNumber registrationNumber);
    PaginatedResult<MaintenanceRecord> findByAircraftRegistration(String aircraftRegistration, int pageNumber, int pageSize);
    Optional<MaintenanceRecord> findByRecordId(UUID recordId);
    Long findVersionFor(UUID recordId);
    Long sumTotalMaintenanceHours();
}
