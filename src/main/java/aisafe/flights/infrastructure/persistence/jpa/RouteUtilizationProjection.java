package aisafe.flights.infrastructure.persistence.jpa;

public interface RouteUtilizationProjection {
    Long getRouteId();
    String getOriginCode();
    String getDestinationCode();
    Long getFlightCount();
}
