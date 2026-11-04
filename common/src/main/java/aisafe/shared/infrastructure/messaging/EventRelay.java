package aisafe.shared.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Sends the events of the {@link EventOutboxEntry outbox} to RabbitMQ, oldest first, and removes
 * each one only once the broker has confirmed it (publisher confirms): an event is never lost, and
 * may be sent twice (the read models ignore a change they already have).
 *
 * <p>It runs right after each commit ({@link #relaySoon()}, in the background so the client does
 * not wait for the broker) and every {@code aisafe.events.relay-interval-ms} (500 ms by default).
 * On the first event that cannot be sent it stops, so the events of this replica always leave in
 * the order they were written, and tries again on the next round.</p>
 */
@Component
public class EventRelay implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(EventRelay.class);
    private static final long CONFIRM_TIMEOUT_MS = 5_000;

    private final EventOutboxRepository outbox;
    private final RabbitTemplate rabbit;
    private final boolean enabled;
    private final ExecutorService background = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "event-relay");
        thread.setDaemon(true);
        return thread;
    });
    /** A run is already queued: the commits in between are sent by it. */
    private final AtomicBoolean queued = new AtomicBoolean();
    /** The broker was unreachable on the last run: logged once, until it comes back. */
    private boolean failing;

    public EventRelay(EventOutboxRepository outbox, RabbitTemplate rabbit,
                      @Value("${aisafe.events.enabled:false}") boolean enabled) {
        this.outbox = outbox;
        this.rabbit = rabbit;
        this.enabled = enabled;
    }

    /** Sends what is waiting as soon as possible, without blocking the caller. */
    public void relaySoon() {
        if (enabled && queued.compareAndSet(false, true)) {
            background.execute(() -> {
                queued.set(false);
                relay();
            });
        }
    }

    @Scheduled(fixedDelayString = "${aisafe.events.relay-interval-ms:500}")
    public void scheduled() {
        if (enabled) {
            relay();
        }
    }

    /** One run: sends the waiting events in order; synchronized so two runs never interleave. */
    synchronized void relay() {
        List<EventOutboxEntry> entries;
        do {
            entries = outbox.findTop100ByOrderByIdAsc();
            for (EventOutboxEntry entry : entries) {
                if (!send(entry)) {
                    return;
                }
            }
        } while (entries.size() == 100);
    }

    private boolean send(EventOutboxEntry entry) {
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        properties.setMessageId("event-" + entry.getId() + "-" + entry.getCreatedAt().toEpochMilli());
        if (entry.getCorrelationId() != null) {
            properties.setHeader(Messaging.CORRELATION_HEADER, entry.getCorrelationId());
        }
        Message message = new Message(entry.getBody().getBytes(StandardCharsets.UTF_8), properties);
        try {
            rabbit.invoke(operations -> {
                operations.send(entry.getExchange(), entry.getRoutingKey(), message);
                operations.waitForConfirmsOrDie(CONFIRM_TIMEOUT_MS);
                return null;
            });
        } catch (RuntimeException e) {
            entry.failed(e.toString());
            outbox.save(entry);
            if (!failing) {
                log.warn("Event {} not published yet ({}); kept in the outbox and retried", entry.getRoutingKey(), e.toString());
                failing = true;
            }
            return false;
        }
        outbox.delete(entry);
        if (failing) {
            log.info("Events published again, the broker is back");
            failing = false;
        }
        log.debug("Event {} published", entry.getRoutingKey());
        return true;
    }

    @Override
    public void destroy() {
        background.shutdownNow();
    }
}
