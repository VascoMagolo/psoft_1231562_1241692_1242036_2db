package aisafe.maintenance.application.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for returning maintenance record information.
 * @param id
 * @param description
 * @param startDate
 * @param expectedDuration
 * @param notes
 * @param partNumbers
 * @param templateName
 * @param status
 * @param aircraftRegistration
 * @param version
 */
public record MaintenanceRecordResponse(
        UUID id,
        String description,
        LocalDateTime startDate,
        Integer expectedDuration,
        String notes,
        List<String> partNumbers,
        String templateName,
        String status,
        String aircraftRegistration,
        Long version
) {}
