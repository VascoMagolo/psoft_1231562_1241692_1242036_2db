package aisafe.maintenance.domain;

import aisafe.DomainException;

public class MaintenancePartAlreadyExistsException extends DomainException {
    public MaintenancePartAlreadyExistsException(String message) {
        super(message);
    }
}
