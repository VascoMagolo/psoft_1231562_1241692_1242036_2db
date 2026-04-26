package aisafe.exceptions;

public class InvalidIataCodeException extends RuntimeException {
    public InvalidIataCodeException(String message) {
        super(message);
    }
}
