package aisafe.shared.infrastructure.messaging;

import org.slf4j.MDC;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

/**
 * Sends a command to another service through RabbitMQ and waits for its answer (request/reply):
 * the exchange {@code aisafe.commands} routes it by the service's name to its queue, where any of
 * its replicas takes it and answers to this replica ({@code reply-to}).
 *
 * <p>A command that no replica takes within {@code aisafe.commands.timeout-ms} (5 s by default)
 * expires in the queue (it goes to the dead-letter queue instead of being carried out late), and
 * the sender gets a {@link CommandUnavailableException}, as with an HTTP call that gets no
 * answer.</p>
 */
@Component
public class CommandBus {

    private final RabbitTemplate rabbit;
    private final ObjectMapper objectMapper;
    private final String service;
    private final long timeoutMs;

    @Autowired
    public CommandBus(ConnectionFactory connectionFactory, ObjectMapper objectMapper,
                      @Value("${spring.application.name}") String service,
                      @Value("${aisafe.commands.timeout-ms:5000}") long timeoutMs) {
        this(replyTemplate(connectionFactory, timeoutMs), objectMapper, service, timeoutMs);
    }

    CommandBus(RabbitTemplate rabbit, ObjectMapper objectMapper, String service, long timeoutMs) {
        this.rabbit = rabbit;
        this.objectMapper = objectMapper;
        this.service = service;
        this.timeoutMs = timeoutMs;
    }

    /** A template of its own: the answer is awaited for as long as the command lives in the queue. */
    private static RabbitTemplate replyTemplate(ConnectionFactory connectionFactory, long timeoutMs) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setReplyTimeout(timeoutMs);
        return template;
    }

    /**
     * @param target  the service that handles it ({@code aircraft-maintenance}...)
     * @param type    the command type ({@code flightcopy.record}...)
     * @param payload the command, sent as JSON
     * @return the answer, whatever its status
     * @throws CommandUnavailableException if no answer arrived in time
     */
    public CommandReply send(String target, String type, Object payload) {
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setHeader(Messaging.COMMAND_TYPE_HEADER, type);
        properties.setHeader(Messaging.SENDER_HEADER, service);
        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            properties.setHeader(Messaging.CORRELATION_HEADER, correlationId);
        }
        // Not carried out late: expires in the queue when nobody takes it before the sender gives up
        properties.setExpiration(Long.toString(timeoutMs));
        Message request = new Message(objectMapper.writeValueAsBytes(payload), properties);
        Message reply;
        try {
            reply = rabbit.sendAndReceive(Messaging.COMMANDS_EXCHANGE, target, request);
        } catch (AmqpException e) {
            throw new CommandUnavailableException("Command " + type + " to " + target + " not sent: " + e.getMessage(), e);
        }
        if (reply == null) {
            throw new CommandUnavailableException("Command " + type + " to " + target + " got no answer in " + timeoutMs + " ms");
        }
        return objectMapper.readValue(reply.getBody(), CommandReply.class);
    }

    /**
     * As {@link #send}, sent once more when no answer arrived: only for commands that change nothing
     * when carried out twice (queries, and the idempotent copies of C4 and C5). The second one goes to
     * another replica of the owner, e.g. when the first was taken by a replica on a VM that just died.
     */
    public CommandReply sendIdempotent(String target, String type, Object payload) {
        try {
            return send(target, type, payload);
        } catch (CommandUnavailableException firstAttempt) {
            return send(target, type, payload);
        }
    }

    /**
     * A query to the owner of some data: the body of its answer, or empty when it does not have it
     * (404, or 400 for a key that cannot exist).
     *
     * @throws CommandUnavailableException if no answer arrived in time, or the owner failed
     */
    public Optional<JsonNode> query(String target, String type, Object payload) {
        CommandReply reply = sendIdempotent(target, type, payload);
        if (reply.ok()) {
            return Optional.ofNullable(reply.body());
        }
        if (reply.status() == 404 || reply.status() == 400) {
            return Optional.empty();
        }
        throw new CommandUnavailableException(type + " to " + target + " answered " + reply.status() + ": " + reply.error());
    }
}
