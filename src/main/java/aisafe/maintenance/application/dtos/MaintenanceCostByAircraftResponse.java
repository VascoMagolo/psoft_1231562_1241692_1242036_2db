package aisafe.maintenance.application.dtos;

import java.math.BigDecimal;

public record MaintenanceCostByAircraftResponse(String aircraftRegistration, BigDecimal totalCost) {}
