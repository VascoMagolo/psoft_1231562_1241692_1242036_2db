package aisafe.shared.infrastructure.messaging;

import aisafe.shared.domain.ConcurrencyException;
import aisafe.shared.domain.DomainException;
import aisafe.shared.domain.DuplicateResourceException;
import aisafe.shared.domain.NotFoundException;
import aisafe.shared.domain.ResourceInUseException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Takes the commands sent to this service ({@code <service>.commands}, shared by its replicas: each
 * command is taken by one of them), runs the {@link CommandHandler} of its type and answers to the
 * sender's {@code reply-to} with a {@link CommandReply}. The domain exceptions become the status an
 * HTTP route would answer ({@code GlobalExceptionHandler}): 404, 409, 412, 400, or 500.
 *
 * <p>Every command is written to the audit log, as the HTTP requests are ({@code AuditFilter}),
 * with the sending service as the user and {@code AMQP} as the method.</p>
 */
@Component
@ConditionalOnProperty(name = "aisafe.events.enabled", havingValue = "true")
public class CommandDispatcher {

    private static final Logger log = LoggerFactory.getLogger(CommandDispatcher.class);
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private final Map<String, CommandHandler> handlers;
    private final ObjectMapper objectMapper;
    private final String nodeId;

    public CommandDispatcher(ObjectProvider<CommandHandler> handlers, ObjectMapper objectMapper,
                             @Value("${aisafe.node.id:local}") String nodeId) {
        this.handlers = handlers.orderedStream().collect(Collectors.toMap(CommandHandler::type, Function.identity()));
        this.objectMapper = objectMapper;
        this.nodeId = nodeId;
    }

    @RabbitListener(queues = "#{@commandQueue.name}")
    public Message onCommand(Message message) {
        long start = System.currentTimeMillis();
        MessageProperties properties = message.getMessageProperties();
        String type = header(properties, Messaging.COMMAND_TYPE_HEADER);
        String sender = header(properties, Messaging.SENDER_HEADER);
        String correlationId = header(properties, Messaging.CORRELATION_HEADER);
        CommandReply reply;
        try (MDC.MDCCloseable ignored = MDC.putCloseable("correlationId", correlationId)) {
            reply = run(type, message.getBody());
            audit(type, sender, correlationId, reply.status(), System.currentTimeMillis() - start);
        }
        MessageProperties answer = new MessageProperties();
        answer.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        return new Message(objectMapper.writeValueAsBytes(reply), answer);
    }

    CommandReply run(String type, byte[] body) {
        CommandHandler handler = type == null ? null : handlers.get(type);
        if (handler == null) {
            return new CommandReply(400, null, "Unknown command: " + type);
        }
        try {
            JsonNode payload = objectMapper.readTree(body);
            Object result = handler.handle(payload);
            return new CommandReply(200, result == null ? null : objectMapper.valueToTree(result), null);
        } catch (RuntimeException e) {
            int status = statusOf(e);
            if (status == 500) {
                log.error("Command {} failed", type, e);
            }
            return new CommandReply(status, null, e.getMessage());
        }
    }

    /** The same status the HTTP routes give these exceptions (GlobalExceptionHandler). */
    static int statusOf(RuntimeException e) {
        if (e instanceof NotFoundException) {
            return 404;
        }
        if (e instanceof DuplicateResourceException || e instanceof ResourceInUseException
                || e instanceof DataIntegrityViolationException || e instanceof ObjectOptimisticLockingFailureException) {
            return 409;
        }
        if (e instanceof ConcurrencyException) {
            return 412;
        }
        if (e instanceof DomainException || e instanceof IllegalArgumentException
                || e instanceof ConstraintViolationException || e instanceof tools.jackson.core.JacksonException) {
            return 400;
        }
        return 500;
    }

    private void audit(String type, String sender, String correlationId, int status, long millis) {
        Map<String, Object> line = new LinkedHashMap<>();
        line.put("time", Instant.now().toString());
        line.put("user", sender == null ? null : "service:" + sender);
        line.put("roles", List.of("SERVICE"));
        line.put("method", "AMQP");
        line.put("path", "command:" + type);
        line.put("status", status);
        line.put("node", nodeId);
        line.put("correlationId", correlationId);
        line.put("forwardedBy", null);
        line.put("ms", millis);
        AUDIT.info(objectMapper.writeValueAsString(line));
    }

    private static String header(MessageProperties properties, String name) {
        Object value = properties.getHeader(name);
        return value == null ? null : value.toString();
    }
}
