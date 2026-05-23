package aisafe.maintenance.application.dtos;

/**
 * Response DTO for returning maintenance template information.
 * @param id
 * @param name
 * @param description
 */
public record MaintenanceTemplateResponse(
        Long id,
        String name,
        String description
) {}