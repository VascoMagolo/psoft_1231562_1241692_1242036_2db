package aisafe.maintenance.domain;

import aisafe.DomainException;

public class MaintenanceRecordAlreadyExistsException extends DomainException {
    public MaintenanceRecordAlreadyExistsException(String message) {
        super(message);
    }
}
