package aisafe.maintenance.application.dtos;

import aisafe.maintenance.domain.MaintenanceComponent;

public record CreateMaintenancePartRequest(String partNumber, String name, String description, Integer stockQuantity, Integer minimumThreshold, MaintenanceComponent component) {
}
