package aisafe.maintenance.application.dtos;

import aisafe.maintenance.domain.MaintenanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record UpdateMaintenanceRecordsRequest(
		@NotNull LocalDateTime startDate,
		@NotBlank String part,
		@NotBlank String template,
		@NotNull MaintenanceStatus status,
		@NotBlank String notes) {
}
