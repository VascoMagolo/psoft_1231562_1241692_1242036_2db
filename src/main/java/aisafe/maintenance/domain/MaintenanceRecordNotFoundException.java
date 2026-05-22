package aisafe.maintenance.domain;

import aisafe.DomainException;

public class MaintenanceRecordNotFoundException extends DomainException {
    public MaintenanceRecordNotFoundException(String message) {
        super(message);
    }
}
