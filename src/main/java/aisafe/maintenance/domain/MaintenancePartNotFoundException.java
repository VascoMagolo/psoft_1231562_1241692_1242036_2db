package aisafe.maintenance.domain;

import aisafe.DomainException;

public class MaintenancePartNotFoundException extends DomainException {
    public MaintenancePartNotFoundException(String message) {
        super(message);
    }
}
