package aisafe.shared.infrastructure.messaging;

/**
 * A command got no answer in time: the broker could not be reached, or no replica of the service
 * took it before it expired. Whether it was carried out is unknown.
 */
public class CommandUnavailableException extends RuntimeException {

    public CommandUnavailableException(String message) {
        super(message);
    }

    public CommandUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
