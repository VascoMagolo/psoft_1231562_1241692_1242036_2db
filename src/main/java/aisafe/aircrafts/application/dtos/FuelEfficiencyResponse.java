package aisafe.aircrafts.application.dtos;

public record FuelEfficiencyResponse(
        String registrationNumber,
        Double fuelConsumptionPerDistanceUnit,
        String routeOrigin,
        String routeDestination,
        Double fuelNeededForRoute
) {}
