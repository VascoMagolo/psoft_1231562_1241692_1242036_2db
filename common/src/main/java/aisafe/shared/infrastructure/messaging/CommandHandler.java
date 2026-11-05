package aisafe.shared.infrastructure.messaging;

import tools.jackson.databind.JsonNode;

/**
 * Handles one type of command sent to this service through RabbitMQ (see {@link CommandBus}).
 * Every bean of this type is found by the {@link CommandDispatcher}.
 *
 * <p>Errors are the domain exceptions the use cases already throw; the dispatcher turns them into
 * the same status an HTTP route would answer (404, 409, 400...).</p>
 */
public interface CommandHandler {

    /** The command type it handles ({@code flightcopy.record}, {@code snapshot}...). */
    String type();

    /** Runs the command; the result is the body of the answer (null for none). */
    Object handle(JsonNode payload);
}
