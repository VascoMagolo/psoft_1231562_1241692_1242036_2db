package aisafe.aircrafts.domain;

import aisafe.DomainException;

public class AircraftModelInvalidFieldException extends DomainException {
    public AircraftModelInvalidFieldException(String message) {
        super(message);
    }
}
