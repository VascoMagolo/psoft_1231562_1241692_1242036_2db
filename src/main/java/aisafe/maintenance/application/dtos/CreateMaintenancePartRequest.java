package aisafe.maintenance.application.dtos;

import aisafe.maintenance.domain.MaintenanceComponent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a new maintenance part.
 * @param partNumber
 * @param name
 * @param description
 * @param stockQuantity
 * @param minimumThreshold
 * @param component
 */
public record CreateMaintenancePartRequest(
		@NotBlank String partNumber,
		@NotBlank String name,
		String description,
		@NotNull @Min(0) Integer stockQuantity,
		@NotNull @Min(0) Integer minimumThreshold,
		@NotNull MaintenanceComponent component) {
}
