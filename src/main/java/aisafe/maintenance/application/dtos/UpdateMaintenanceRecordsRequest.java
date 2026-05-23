package aisafe.maintenance.application.dtos;

import aisafe.maintenance.domain.MaintenanceStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating maintenance records.
 * @param status
 * @param notes
 */
public record UpdateMaintenanceRecordsRequest(
		@NotNull MaintenanceStatus status,
		String notes) {
}
