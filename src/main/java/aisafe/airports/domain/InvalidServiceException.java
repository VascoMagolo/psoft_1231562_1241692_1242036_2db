package aisafe.airports.domain;

import aisafe.shared.domain.DomainException;

public class InvalidServiceException extends DomainException {
    public InvalidServiceException(String message) {
        super(message);
    }
}
