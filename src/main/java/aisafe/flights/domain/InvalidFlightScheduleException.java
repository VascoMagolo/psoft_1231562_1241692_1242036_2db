package aisafe.flights.domain;

import aisafe.shared.domain.DomainException;

/**
 * Raised when a flight schedule is invalid (e.g. departure after arrival).
 */
public class InvalidFlightScheduleException extends DomainException {
    public InvalidFlightScheduleException(String message) {
        super(message);
    }
}
