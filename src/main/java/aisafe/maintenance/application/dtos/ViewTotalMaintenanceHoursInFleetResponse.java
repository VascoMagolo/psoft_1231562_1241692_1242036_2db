package aisafe.maintenance.application.dtos;

/**
 * Response DTO for returning the total maintenance hours in the fleet.
 * @param totalHours
 */
public record ViewTotalMaintenanceHoursInFleetResponse(Integer totalHours) {
}
