package aisafe.aircrafts.domain;

import aisafe.shared.domain.DomainException;

/**
 * Raised when the requested aircraft model cannot be found.
 */
public class AircraftModelNotFoundException extends DomainException {
    public AircraftModelNotFoundException(String message) {
        super(message);
    }
}
