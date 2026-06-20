package aisafe.airports.domain;

import aisafe.shared.domain.DomainException;

public class InvalidRunwayException extends DomainException {
    public InvalidRunwayException(String message) {
        super(message);
    }
}
