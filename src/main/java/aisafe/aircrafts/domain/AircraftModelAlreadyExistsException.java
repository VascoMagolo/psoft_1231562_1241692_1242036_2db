package aisafe.aircrafts.domain;

import aisafe.DomainException;

/**
 * Raised when an aircraft model with the same name already exists.
 */
public class AircraftModelAlreadyExistsException extends DomainException {
    public AircraftModelAlreadyExistsException(String message) {
        super(message);
    }
}
