package aisafe.maintenance.domain;

import aisafe.shared.domain.DomainException;

/**
 * Thrown when a requested maintenance part cannot be found in the system.
 */
public class MaintenancePartNotFoundException extends DomainException {
    public MaintenancePartNotFoundException(String message) {
        super(message);
    }
}
