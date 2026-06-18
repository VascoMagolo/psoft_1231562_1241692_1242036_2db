package aisafe.flights.domain;

import aisafe.shared.domain.DomainException;

public class InvalidFlightDateRangeException extends DomainException {
    public InvalidFlightDateRangeException(String message) {
        super(message);
    }
}
