package aisafe.shared.infrastructure.readmodel;

import aisafe.shared.infrastructure.messaging.ChangeEvent;
import aisafe.shared.infrastructure.messaging.Messaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Applies the events this replica's queue receives to its read model: the events of this service
 * (every replica has its own queue bound to the events exchange, so every replica receives every
 * event, also the ones of changes made on this replica), and those of other services it reads
 * ({@link ExternalProjection}).
 *
 * <p>A message that cannot be applied is not requeued forever
 * ({@code spring.rabbitmq.listener.simple.default-requeue-rejected=false}): it goes to the
 * dead-letter queue of this service, and the reconciliation or the next snapshot repairs the row.</p>
 */
@Component
@ConditionalOnProperty(name = "aisafe.events.enabled", havingValue = "true")
public class ReadModelListener {

    private static final Logger log = LoggerFactory.getLogger(ReadModelListener.class);

    private final ReadModelStore store;
    private final ObjectMapper objectMapper;

    public ReadModelListener(ReadModelStore store, ObjectMapper objectMapper) {
        this.store = store;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "#{@readModelQueue.name}")
    public void onEvent(Message message) {
        ChangeEvent event = objectMapper.readValue(message.getBody(), ChangeEvent.class);
        Object correlationId = message.getMessageProperties().getHeader(Messaging.CORRELATION_HEADER);
        try (MDC.MDCCloseable ignored = MDC.putCloseable("correlationId", correlationId == null ? null : correlationId.toString())) {
            String entity = event.type().substring(0, event.type().indexOf('.'));
            JsonNode view = event.data() == null ? null : objectMapper.valueToTree(event.data());
            boolean applied = store.apply(entity, event.key(), view, event.version(), event.stamp(), event.stampNode());
            log.info("Event {} {} from {} {} to the read model", event.type(), event.key(), event.nodeId(),
                    applied ? "applied" : "already older than");
        }
    }
}
