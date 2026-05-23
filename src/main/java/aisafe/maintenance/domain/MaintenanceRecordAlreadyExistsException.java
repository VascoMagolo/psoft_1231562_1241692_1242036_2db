package aisafe.maintenance.domain;

import aisafe.DomainException;

/**
 * Thrown when an attempt is made to add a maintenance part that already exists in the system.
 */
public class MaintenanceRecordAlreadyExistsException extends DomainException {
    public MaintenanceRecordAlreadyExistsException(String message) {
        super(message);
    }
}
