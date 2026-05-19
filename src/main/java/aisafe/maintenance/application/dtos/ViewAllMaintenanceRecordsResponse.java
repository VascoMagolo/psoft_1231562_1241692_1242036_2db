package aisafe.maintenance.application.dtos;

import aisafe.maintenance.domain.MaintenanceStatus;

import java.time.LocalDateTime;

public record ViewAllMaintenanceRecordsResponse(String partNumber, String name, LocalDateTime startDate, Integer expectedDuration, MaintenanceStatus status, String notes, String number) {

}
