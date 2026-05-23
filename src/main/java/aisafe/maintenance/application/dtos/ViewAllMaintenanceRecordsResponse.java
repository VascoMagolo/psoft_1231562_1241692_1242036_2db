package aisafe.maintenance.application.dtos;

import aisafe.maintenance.domain.MaintenanceStatus;

import java.time.LocalDateTime;

/**
 * Response DTO for returning maintenance record information in the "View All Maintenance Records" use case.
 * @param partNumber
 * @param name
 * @param startDate
 * @param expectedDuration
 * @param status
 * @param notes
 * @param number
 */
public record ViewAllMaintenanceRecordsResponse(String partNumber, String name, LocalDateTime startDate, Integer expectedDuration, MaintenanceStatus status, String notes, String number) {

}
