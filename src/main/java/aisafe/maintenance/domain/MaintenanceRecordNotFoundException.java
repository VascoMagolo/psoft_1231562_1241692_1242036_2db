package aisafe.maintenance.domain;

import aisafe.shared.domain.DomainException;

/**
 * Thrown when a requested maintenance part cannot be found in the system.
 */
public class MaintenanceRecordNotFoundException extends DomainException {
    public MaintenanceRecordNotFoundException(String message) {
        super(message);
    }
}
