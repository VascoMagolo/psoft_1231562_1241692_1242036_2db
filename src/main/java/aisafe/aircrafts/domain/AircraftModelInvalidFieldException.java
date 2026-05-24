package aisafe.aircrafts.domain;

import aisafe.shared.domain.DomainException;

/**
 * Raised when aircraft model data fails a business validation rule.
 */
public class AircraftModelInvalidFieldException extends DomainException {
    public AircraftModelInvalidFieldException(String message) {
        super(message);
    }
}
