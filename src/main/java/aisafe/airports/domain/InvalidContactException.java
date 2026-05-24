package aisafe.airports.domain;

import aisafe.shared.domain.DomainException;

/**
 * Raised when the contact information provided for an airport is invalid
 */
public class InvalidContactException extends DomainException {
    public InvalidContactException(String message) {
        super(message);
    }
}
