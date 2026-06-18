package aisafe.maintenance.domain;

import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.shared.domain.BaseRepository;
import aisafe.shared.domain.PaginatedResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
    PaginatedResult<MaintenanceRecord> search(
            RegistrationNumber aircraftRegistration,
            LocalDateTime from,
            LocalDateTime to,
            MaintenanceComponent component,
            int pageNumber,
            int pageSize
    );
    PaginatedResult<MaintenanceRecord> findByStatus(MaintenanceStatus status, int pageNumber, int pageSize);
    BigDecimal sumCostByAircraftRegistration(RegistrationNumber registration);
    BigDecimal sumCostByRegistrations(List<String> registrationNumbers);
    MaintenanceTurnaroundData findAverageTurnaroundByRegistrations(String modelName, List<String> registrations);
}
