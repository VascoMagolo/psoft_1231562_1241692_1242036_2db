package aisafe.maintenance.application.dtos;

/**
 * Response DTO for returning maintenance part information.
 * @param id
 * @param partNumber
 * @param description
 */
public record MaintenancePartResponse(
        Long id,
        String partNumber,
        String description
) {}