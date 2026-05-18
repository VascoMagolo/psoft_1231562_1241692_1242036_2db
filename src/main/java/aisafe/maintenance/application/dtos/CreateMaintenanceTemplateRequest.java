package aisafe.maintenance.application.dtos;

import aisafe.maintenance.domain.MaintenanceType;

import java.util.List;

/**
 * Request payload for creating a maintenance template.
 * Applicable aircraft models are sent as model names and resolved by the use case.
 */
public record CreateMaintenanceTemplateRequest(
		String name,
		MaintenanceType templateType,
		List<String> applicableModels,
		List<String> checklist,
		Integer intervalFlightHours,
		Integer intervalDays) {
}
