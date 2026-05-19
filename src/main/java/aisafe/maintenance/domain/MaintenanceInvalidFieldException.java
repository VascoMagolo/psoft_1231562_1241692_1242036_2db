package aisafe.maintenance.domain;

import aisafe.DomainException;

public class MaintenanceInvalidFieldException extends DomainException {
    public MaintenanceInvalidFieldException(String message) {
        super(message);
    }
}
