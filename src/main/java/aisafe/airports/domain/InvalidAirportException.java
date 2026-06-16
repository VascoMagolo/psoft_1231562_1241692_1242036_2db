package aisafe.airports.domain;

import aisafe.shared.domain.DomainException;

public class InvalidAirportException extends DomainException {
    public InvalidAirportException(String message) {
        super(message);
    }
}
