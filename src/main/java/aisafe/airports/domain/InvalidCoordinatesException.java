package aisafe.airports.domain;

import aisafe.shared.domain.DomainException;

public class InvalidCoordinatesException extends DomainException {
    public InvalidCoordinatesException(String message) {
        super(message);
    }
}
