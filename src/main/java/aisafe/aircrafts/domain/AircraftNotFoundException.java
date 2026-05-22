package aisafe.aircrafts.domain;

import aisafe.DomainException;

/**
 * Raised when the requested aircraft cannot be found.
 */
public class AircraftNotFoundException extends DomainException {
    public AircraftNotFoundException(String message) {
        super(message);
    }
}
