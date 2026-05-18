package aisafe.maintenance.application.dtos;

import aisafe.maintenance.domain.MaintenanceStatus;

import java.time.LocalDateTime;

public record UpdateMaintenanceRecordsRequest(LocalDateTime startDate, String part, String template, MaintenanceStatus status, String notes) {
}
