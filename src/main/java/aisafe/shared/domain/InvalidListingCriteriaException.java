package aisafe.shared.domain;

/**
 * Raised when listing criteria are invalid.
 */
public class InvalidListingCriteriaException extends DomainException {
    public InvalidListingCriteriaException(String message) {
        super(message);
    }
}
