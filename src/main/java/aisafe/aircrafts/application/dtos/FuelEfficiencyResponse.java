package aisafe.aircrafts.application.dtos;

public record FuelEfficiencyResponse(
        String registrationNumber,
        Double fuelConsumptionPerDistanceUnit,
        Long routeId,
        Double fuelNeededForRoute
) {}
