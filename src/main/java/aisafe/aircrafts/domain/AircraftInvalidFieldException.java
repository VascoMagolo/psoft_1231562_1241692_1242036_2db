package aisafe.aircrafts.domain;

import aisafe.DomainException;

/**
 * Raised when aircraft data fails a business validation rule.
 */

public class AircraftInvalidFieldException extends DomainException {
    public AircraftInvalidFieldException(String message) {
        super(message);
    }
}
