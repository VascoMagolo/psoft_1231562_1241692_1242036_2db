package aisafe.maintenance.domain;

import aisafe.DomainException;

/**
 * Exception thrown when attempting to create a maintenance template that already exists in the system.
 */
public class MaintenanceTemplateAlreadyExistsException extends DomainException {
    public MaintenanceTemplateAlreadyExistsException(String message) {
        super(message);
    }
}
