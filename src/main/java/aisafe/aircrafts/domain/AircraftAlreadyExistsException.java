package aisafe.aircrafts.domain;

import aisafe.DomainException;

/**
 * Raised when an aircraft with the same registration number already exists.
 */
public class AircraftAlreadyExistsException extends DomainException {
    public AircraftAlreadyExistsException(String message) {
        super(message);
    }
}
