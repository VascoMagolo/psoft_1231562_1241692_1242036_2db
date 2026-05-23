package aisafe.maintenance.application.dtos;

import java.time.LocalDateTime;

/**
 * Response DTO for returning maintenance record information.
 * @param id
 * @param description
 * @param startDate
 * @param expectedDuration
 * @param notes
 * @param partNumber
 * @param templateName
 * @param status
 * @param aircraftRegistration
 * @param version
 */
public record MaintenanceRecordResponse(
        Long id,
        String description,
        LocalDateTime startDate,
        Integer expectedDuration,
        String notes,
        String partNumber,
        String templateName,
        String status,
        String aircraftRegistration,
        Long version
) {}