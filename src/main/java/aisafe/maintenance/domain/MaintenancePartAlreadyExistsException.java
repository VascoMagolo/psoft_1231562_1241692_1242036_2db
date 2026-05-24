package aisafe.maintenance.domain;

import aisafe.shared.domain.DomainException;

/**
 * Thrown when an attempt is made to add a maintenance part that already exists in the system.
 */
public class MaintenancePartAlreadyExistsException extends DomainException {
    public MaintenancePartAlreadyExistsException(String message) {
        super(message);
    }
}
