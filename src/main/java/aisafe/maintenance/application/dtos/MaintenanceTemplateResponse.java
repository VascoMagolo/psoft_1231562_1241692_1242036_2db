package aisafe.maintenance.application.dtos;

/**
 * Response DTO for returning maintenance template information.
 * @param name
 * @param description
 */
public record MaintenanceTemplateResponse(
        String name,
        String description
) {}
