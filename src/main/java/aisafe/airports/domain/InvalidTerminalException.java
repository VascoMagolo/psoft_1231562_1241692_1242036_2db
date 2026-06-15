package aisafe.airports.domain;

import aisafe.shared.domain.DomainException;

public class InvalidTerminalException extends DomainException {
    public InvalidTerminalException(String message) {
        super(message);
    }
}
