package aisafe.maintenance.application.dtos;

import java.util.List;

public record UpdateMaintenanceTemplateRequest(
        List<String> checklist,
        Integer intervalFlightHours,
        Integer intervalDays
) {}
