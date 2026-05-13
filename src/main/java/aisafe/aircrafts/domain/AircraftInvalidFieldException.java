package aisafe.aircrafts.domain;
/**
 * Raised when aircraft data fails a business validation rule.
 */

public class AircraftInvalidFieldException extends RuntimeException {
    public AircraftInvalidFieldException(String message) {
        super(message);
    }
}
