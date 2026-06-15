package aisafe.flights.domain;

import aisafe.shared.domain.DomainException;

/**
 * Raised when an aircraft is incompatible with a route (e.g. range exceeded).
 */
public class AircraftIncompatibilityException extends DomainException {
    public AircraftIncompatibilityException(String message) {
        super(message);
    }
}
