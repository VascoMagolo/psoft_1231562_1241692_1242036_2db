package aisafe.shared.domain;

/**
 * Raised when an optimistic locking conflict occurs, typically due to an If-Match header mismatch.
 */
public class ConcurrencyException extends DomainException {
    public ConcurrencyException(String message) {
        super(message);
    }
}
