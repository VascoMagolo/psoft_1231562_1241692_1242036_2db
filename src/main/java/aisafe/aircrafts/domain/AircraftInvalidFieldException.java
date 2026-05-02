package aisafe.aircrafts.domain;

public class AircraftInvalidFieldException extends RuntimeException {
    public AircraftInvalidFieldException(String message) {
        super(message);
    }
}
