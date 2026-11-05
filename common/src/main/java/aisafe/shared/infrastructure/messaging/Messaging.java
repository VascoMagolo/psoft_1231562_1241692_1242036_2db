package aisafe.shared.infrastructure.messaging;

/**
 * Names on RabbitMQ shared by every service, so a service can find another's exchange and queues
 * from its name alone.
 *
 * <ul>
 *   <li>{@code <service>.events}: topic exchange where a service publishes its events
 *   ({@code <entity>.<change>}, e.g. {@code aircraft.updated});</li>
 *   <li>{@code <service>.read-model.<replica>}: each replica's queue for its read model;</li>
 *   <li>{@code aisafe.commands} (direct): commands, routed by the name of the service that handles
 *   them to its queue {@code <service>.commands}, shared by its replicas;</li>
 *   <li>{@code aisafe.dead-letter} (direct): messages that could not be handled, routed by service
 *   name to {@code <service>.dead-letter}.</li>
 * </ul>
 */
public final class Messaging {

    public static final String COMMANDS_EXCHANGE = "aisafe.commands";
    public static final String DEAD_LETTER_EXCHANGE = "aisafe.dead-letter";
    /** The client's correlation id ({@code X-Correlation-Id}), carried by every message. */
    public static final String CORRELATION_HEADER = "x-correlation-id";
    /** The service that sent a command. */
    public static final String SENDER_HEADER = "x-sender";
    /** What a command asks for ({@code flightcopy.record}, {@code snapshot}...). */
    public static final String COMMAND_TYPE_HEADER = "x-command-type";

    private Messaging() {
    }

    public static String eventsExchange(String service) {
        return service + ".events";
    }

    public static String readModelQueue(String service, String nodeId) {
        return service + ".read-model." + nodeId;
    }

    public static String commandQueue(String service) {
        return service + ".commands";
    }

    public static String deadLetterQueue(String service) {
        return service + ".dead-letter";
    }
}
