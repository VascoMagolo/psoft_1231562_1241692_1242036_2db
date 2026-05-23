package aisafe.airports.domain;

import aisafe.DomainException;

/**
 * Raised when the requested airport cannot be found.
 */
public class AirportNotFoundException extends DomainException {
    public AirportNotFoundException(String iataCode) {
        super("Airport with IATA code '" + iataCode + "' not found.");
    }
}
