package aisafe.routes.domain;

import aisafe.DomainException;

public class RouteNotFoundException extends DomainException {
    public RouteNotFoundException(String id) {
        super("Route with ID " + id + " was not found.");
    }
}