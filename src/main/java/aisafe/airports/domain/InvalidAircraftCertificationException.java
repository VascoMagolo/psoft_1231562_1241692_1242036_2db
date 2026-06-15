package aisafe.airports.domain;

import aisafe.shared.domain.DomainException;

public class InvalidAircraftCertificationException extends DomainException {
    public InvalidAircraftCertificationException(String message) {
        super(message);
    }
}
