package aisafe.maintenance.domain;

import aisafe.DomainException;

/**
 * Thrown when an attempt is made to create or update a maintenance part with invalid field values
 */
public class MaintenanceInvalidFieldException extends DomainException {
    public MaintenanceInvalidFieldException(String message) {
        super(message);
    }
}
