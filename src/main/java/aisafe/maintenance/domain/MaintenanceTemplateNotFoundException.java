package aisafe.maintenance.domain;

import aisafe.DomainException;

public class MaintenanceTemplateNotFoundException extends DomainException {
    public MaintenanceTemplateNotFoundException(String message) {
        super(message);
    }
}
