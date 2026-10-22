package aisafe.airportsroutes.infrastructure.messaging;

import aisafe.shared.infrastructure.replication.ReplicaState;
import aisafe.shared.infrastructure.replication.ReplicaStateRepository;
import aisafe.shared.infrastructure.replication.Replicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * Publishes to RabbitMQ what changed in this service: an airport or a route created, updated or
 * deleted. Called by the persistence adapters, right after the {@link Replicator}, but only for
 * changes made on this replica: a copy received from a peer was already published by the replica
 * that made the change.
 *
 * <p>Sent to the topic exchange {@value #EXCHANGE} once the change is committed, with routing keys
 * such as {@code route.created} or {@code airport.deleted}. An event carries the stamp the
 * {@link Replicator} gave the change (time and replica), so whoever applies events and replicas
 * keeps the same latest change. Off unless {@code aisafe.events.enabled=true}, so the service still
 * runs without a broker. If the broker cannot be reached the event is only logged: the change itself
 * is kept.</p>
 */
@Component
public class EventPublisher {

    public static final String EXCHANGE = "airports-routes.events";

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final RabbitTemplate rabbit;
    private final Replicator replicator;
    private final ReplicaStateRepository states;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final String nodeId;
    private final Clock clock;

    @Autowired
    public EventPublisher(RabbitTemplate rabbit, Replicator replicator, ReplicaStateRepository states,
                          ObjectMapper objectMapper,
                          @Value("${aisafe.events.enabled:false}") boolean enabled,
                          @Value("${aisafe.node.id:local}") String nodeId) {
        this(rabbit, replicator, states, objectMapper, enabled, nodeId, Clock.systemUTC());
    }

    EventPublisher(RabbitTemplate rabbit, Replicator replicator, ReplicaStateRepository states,
                   ObjectMapper objectMapper, boolean enabled, String nodeId, Clock clock) {
        this.rabbit = rabbit;
        this.replicator = replicator;
        this.states = states;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.nodeId = nodeId;
        this.clock = clock;
    }

    /**
     * @param replicaType replication type of the entity ({@code airports}, {@code routes}), to find
     *                    the stamp of the change
     * @param entity      {@code airport} or {@code route}
     * @param change      {@code created}, {@code updated} or {@code deleted}
     * @param key         IATA code of the airport, or {@code ORIGIN-DESTINATION} of the route
     * @param status      status after the change, null when deleted
     * @param version     version after the change (the one its ETag is built from), if any
     * @param data        the entity after the change, null when deleted or not needed by anyone
     */
    public void changed(String replicaType, String entity, String change, String key, Enum<?> status,
                        Long version, Object data) {
        if (!enabled || replicator.isApplyingReplica()) {
            return;
        }
        // Same id as the Replicator's state of the entity; without one (no peers), stamped now, here
        Optional<ReplicaState> state = states.findById(replicaType + ":" + key);
        ChangeEvent event = new ChangeEvent(entity + "." + change, key, status == null ? null : status.name(),
                version, data,
                state.map(ReplicaState::getStamp).orElseGet(clock::millis),
                state.map(ReplicaState::getStampNode).orElse(nodeId),
                nodeId, Instant.now(clock));
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            send(event);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                send(event);
            }
        });
    }

    private void send(ChangeEvent event) {
        try {
            MessageProperties properties = new MessageProperties();
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            rabbit.send(EXCHANGE, event.type(), new Message(objectMapper.writeValueAsBytes(event), properties));
            log.info("Event {} {} published", event.type(), event.key());
        } catch (RuntimeException e) {
            log.warn("Event {} {} not published ({})", event.type(), event.key(), e.toString());
        }
    }

    /** Body of every event, as JSON. */
    public record ChangeEvent(String type, String key, String status, Long version, Object data,
                              long stamp, String stampNode, String nodeId, Instant occurredAt) {
    }
}
