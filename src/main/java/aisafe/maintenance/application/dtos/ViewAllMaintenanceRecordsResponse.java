package aisafe.maintenance.application.dtos;

import aisafe.maintenance.domain.MaintenanceStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Response DTO for returning maintenance record information in the "View All Maintenance Records" use case.
 * @param partNumbers
 * @param name
 * @param startDate
 * @param expectedDuration
 * @param status
 * @param notes
 * @param number
 * @param components
 */
public record ViewAllMaintenanceRecordsResponse(List<String> partNumbers, String name, LocalDateTime startDate, Integer expectedDuration, MaintenanceStatus status, String notes, String number, Set<String> components) {

}
