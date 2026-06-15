package aisafe.routes.domain;

import aisafe.shared.domain.DomainException;

/**
 * Raised when a route violates business rules (e.g. same origin and destination).
 */
public class InvalidRouteException extends DomainException {
    public InvalidRouteException(String message) {
        super(message);
    }
}
