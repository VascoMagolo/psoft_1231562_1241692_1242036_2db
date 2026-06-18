package aisafe.flights.domain;

public record RouteUtilizationData(
    Long routeId,
    String origin,
    String destination,
    Long count
) {}
