package aisafe.maintenance.domain;

import aisafe.shared.domain.DomainException;

/**
 * Thrown when a requested maintenance template cannot be found in the system
 */
public class MaintenanceTemplateNotFoundException extends DomainException {
    public MaintenanceTemplateNotFoundException(String message) {
        super(message);
    }
}
