package aisafe.flights.application.dtos;

public record RouteUtilizationResponse(
    Long routeId,
    String origin,
    String destination,
    Long count
) {}
