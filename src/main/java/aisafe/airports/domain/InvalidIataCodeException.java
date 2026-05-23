package aisafe.airports.domain;

import aisafe.DomainException;

/**
 * Raised when an invalid IATA code is provided for an airport.
 */
public class InvalidIataCodeException extends DomainException {
    public InvalidIataCodeException(String message) {
        super(message);
    }
}
