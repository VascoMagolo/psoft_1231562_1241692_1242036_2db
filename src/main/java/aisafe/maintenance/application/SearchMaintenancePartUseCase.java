package aisafe.maintenance.application;

import aisafe.maintenance.application.dtos.MaintenancePartResponse;
import aisafe.maintenance.domain.MaintenanceComponent;
import aisafe.maintenance.domain.MaintenancePart;
import aisafe.maintenance.domain.MaintenancePartRepository;
import aisafe.shared.application.UseCase;
import aisafe.shared.domain.PaginatedResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Use case for searching maintenance parts based on various criteria.
 * Covers US226 requirements for tracking inventory and low-stock alerts.
 */
@UseCase(readOnly = true)
public class SearchMaintenancePartUseCase {
    private final MaintenancePartRepository maintenancePartRepository;

    public SearchMaintenancePartUseCase(MaintenancePartRepository maintenancePartRepository) {
        this.maintenancePartRepository = maintenancePartRepository;
    }

    /**
     * Executes the search for maintenance parts.
     * 
     * @param partNumber   (optional) Filter by part number
     * @param name         (optional) Filter by name
     * @param component    (optional) Filter by component
     * @param lowStockOnly (optional) If true, only returns parts with stock below threshold
     * @param pageNumber   Page index
     * @param pageSize     Page size
     * @return Paginated result of maintenance part responses
     */
    public PaginatedResult<MaintenancePartResponse> execute(
            String partNumber, String name, MaintenanceComponent component, 
            Boolean lowStockOnly, int pageNumber, int pageSize) {
        
        PaginatedResult<MaintenancePart> domainResult = maintenancePartRepository.searchParts(
                partNumber, name, component, lowStockOnly, pageNumber, pageSize);

        List<MaintenancePartResponse> dtos = domainResult.data().stream()
                .map(MaintenancePartResponse::from)
                .collect(Collectors.toList());

        return new PaginatedResult<>(dtos, domainResult.totalElements());
    }
}
