package aisafe.maintenance.domain;

import aisafe.DomainException;

public class MaintenanceTemplateAlreadyExistsException extends DomainException {
    public MaintenanceTemplateAlreadyExistsException(String message) {
        super(message);
    }
}
