package aisafe.maintenance.application.dtos;

import aisafe.maintenance.domain.MaintenanceComponent;
import aisafe.maintenance.domain.MaintenancePart;

/**
 * Response DTO for returning maintenance part information.
 */
public record MaintenancePartResponse(
        String partNumber,
        String name,
        String description,
        Integer stockQuantity,
        Integer minimumThreshold,
        MaintenanceComponent component
) {
    public static MaintenancePartResponse from(MaintenancePart part) {
        return new MaintenancePartResponse(
                part.getPartNumber(),
                part.getName(),
                part.getDescription(),
                part.getStockQuantity(),
                part.getMinimumThreshold(),
                part.getComponent()
        );
    }
}
