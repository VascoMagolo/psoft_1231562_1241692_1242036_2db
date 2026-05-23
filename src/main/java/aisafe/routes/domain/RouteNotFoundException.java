package aisafe.routes.domain;

import aisafe.DomainException;

/**
 * Raised when the requested route cannot be found.
 */
public class RouteNotFoundException extends DomainException {
    public RouteNotFoundException(String id) {
        super("Route with ID " + id + " was not found.");
    }
}