package aisafe.shared.domain;

/**
 * Raised when a resource cannot be deleted or modified because it is currently in use by other resources.
 */
public class ResourceInUseException extends DomainException {
    public ResourceInUseException(String message) {
        super(message);
    }
}
