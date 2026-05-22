package aisafe.maintenance.application.dtos;

import aisafe.maintenance.domain.MaintenanceComponent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMaintenancePartRequest(
		@NotBlank String partNumber,
		@NotBlank String name,
		String description,
		@NotNull @Min(0) Integer stockQuantity,
		@NotNull @Min(0) Integer minimumThreshold,
		@NotNull MaintenanceComponent component) {
}
