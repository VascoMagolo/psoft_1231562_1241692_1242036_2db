package aisafe.maintenance.application.dtos;

public record UpdateMaintenancePartRequest(
        String description,
        Integer stockQuantity,
        Integer minimumThreshold
) {}
