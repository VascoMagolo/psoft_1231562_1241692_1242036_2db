package aisafe.airports.domain;

import aisafe.shared.domain.DomainException;

public class InvalidGateException extends DomainException {
    public InvalidGateException(String message) {
        super(message);
    }
}
