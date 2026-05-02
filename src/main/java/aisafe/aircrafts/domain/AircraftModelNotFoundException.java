package aisafe.aircrafts.domain;

import aisafe.DomainException;

public class AircraftModelNotFoundException extends DomainException {
    public AircraftModelNotFoundException(String message) {
        super(message);
    }
}
