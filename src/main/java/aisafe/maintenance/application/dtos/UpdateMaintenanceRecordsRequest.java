package aisafe.maintenance.application.dtos;

import aisafe.maintenance.domain.MaintenanceStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateMaintenanceRecordsRequest(
		@NotNull MaintenanceStatus status,
		String notes) {
}
