package aisafe.maintenance.application.dtos;

public record MaintenanceDueAircraftResponse(
        String registrationNumber,
        String modelName,
        String dueReason,
        Double accumulatedFlightHours,
        Long elapsedDays,
        String templateName
) {}
