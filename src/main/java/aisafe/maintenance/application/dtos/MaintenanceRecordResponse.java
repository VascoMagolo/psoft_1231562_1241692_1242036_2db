package aisafe.maintenance.application.dtos;

import java.time.LocalDateTime;

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