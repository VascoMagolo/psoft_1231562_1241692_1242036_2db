package aisafe.maintenance.application.dtos;

import aisafe.maintenance.domain.MaintenanceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request payload for creating a maintenance template.
 * Applicable aircraft models are sent as model names and resolved by the use case.
 */
public record CreateMaintenanceTemplateRequest(
		@NotBlank String name,
		@NotNull MaintenanceType templateType,
		@NotEmpty List<@NotBlank String> applicableModels,
		@NotEmpty List<@NotBlank String> checklist,
		@NotNull @Min(1) Integer intervalFlightHours,
		@NotNull @Min(1) Integer intervalDays) {
}
