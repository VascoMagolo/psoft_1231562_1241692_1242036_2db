package aisafe.flights.infrastructure.persistence;

public interface RouteUtilizationProjection {
    Long getRouteId();
    String getOriginCode();
    String getDestinationCode();
    Long getFlightCount();
}
