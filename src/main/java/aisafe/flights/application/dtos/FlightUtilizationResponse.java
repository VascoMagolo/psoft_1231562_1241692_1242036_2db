package aisafe.flights.application.dtos;

public record FlightUtilizationResponse(
    Long routeId,
    String origin,
    String destination,
    Long count
) {}
