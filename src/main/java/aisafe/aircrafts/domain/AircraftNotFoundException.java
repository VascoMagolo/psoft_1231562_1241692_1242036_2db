package aisafe.aircrafts.domain;

import aisafe.DomainException;

public class AircraftNotFoundException extends DomainException {
    public AircraftNotFoundException(String message) {
        super(message);
    }
}
