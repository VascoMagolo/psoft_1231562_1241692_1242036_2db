package aisafe.routes.domain;

import aisafe.shared.domain.DomainException;

/**
 * Raised when an invalid sort parameter is provided for route listing.
 */
public class InvalidSortParameterException extends DomainException {
    public InvalidSortParameterException(String message) {
        super(message);
    }
}
