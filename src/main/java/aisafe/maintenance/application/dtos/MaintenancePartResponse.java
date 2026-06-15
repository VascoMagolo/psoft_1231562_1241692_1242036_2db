package aisafe.maintenance.application.dtos;

/**
 * Response DTO for returning maintenance part information.
 * @param partNumber
 * @param description
 */
public record MaintenancePartResponse(
        String partNumber,
        String description
) {}
