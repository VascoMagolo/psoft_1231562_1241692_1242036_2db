package aisafe.maintenance.application.dtos;

import aisafe.maintenance.domain.MaintenanceComponent;

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
    public static MaintenancePartResponse from(aisafe.maintenance.domain.MaintenancePart part) {
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
