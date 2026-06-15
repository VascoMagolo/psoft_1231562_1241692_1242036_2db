package aisafe.aircrafts.application.dtos;

public record CompatibleRouteResponse(String originIataCode, String destinationIataCode, Integer estimatedFlightTime, Double minimumRange, Integer minimumCapacity) {}
