package aisafe.aircrafts.domain;

import aisafe.shared.domain.DomainException;

public class AircraftModelImageNotFoundException extends DomainException {
    public AircraftModelImageNotFoundException(String message) {
        super(message);
    }
}
