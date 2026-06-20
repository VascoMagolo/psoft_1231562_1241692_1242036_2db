package aisafe.maintenance.application.dtos;

import java.math.BigDecimal;

public record MaintenanceCostByModelResponse(String modelName, BigDecimal totalCost) {}
