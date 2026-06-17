package aisafe.maintenance.domain;

import aisafe.shared.domain.BaseRepository;
import aisafe.shared.domain.PaginatedResult;

import java.util.Optional;

public interface MaintenancePartRepository extends BaseRepository<MaintenancePart> {
    boolean existsByPartNumber(String partNumber);
    Optional<MaintenancePart> findByPartNumber(String partNumber);
    PaginatedResult<MaintenancePart> searchParts(String partNumber, String name, MaintenanceComponent component, Boolean lowStockOnly, int pageNumber, int pageSize);
}
