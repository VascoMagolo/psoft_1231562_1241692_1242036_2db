package aisafe.shared.infrastructure.messaging;

import aisafe.shared.infrastructure.replication.ReplicaState;
import aisafe.shared.infrastructure.replication.ReplicaStateRepository;
import aisafe.shared.infrastructure.replication.Replicator;
import org.slf4j.MDC;
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
 * Publishes what changed in this service: an entity created, updated or deleted. Called by the
 * persistence adapters, right after the {@link Replicator}, but only for changes made on this
 * replica: a copy received from a peer was already published by the replica that made the change.
 *
 * <p>The event is written to the {@link EventOutboxEntry outbox} in the transaction of the change
 * (transactional outbox): if the change is committed, so is its event, and the {@link EventRelay}
 * sends it to the exchange {@code <service>.events} (routing key {@code <entity>.<change>}) as soon
 * as the change is committed, or later if the broker cannot be reached. An event carries the stamp
 * the {@link Replicator} gave the change (time and replica), so whoever applies events and replicas
 * keeps the same latest change. Off unless {@code aisafe.events.enabled=true}, so the service still
 * runs without a broker.</p>
 */
@Component
public class EventPublisher {

    private final EventOutboxRepository outbox;
    private final EventRelay relay;
    private final Replicator replicator;
    private final ReplicaStateRepository states;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final String nodeId;
    private final String exchange;
    private final Clock clock;

    @Autowired
    public EventPublisher(EventOutboxRepository outbox, EventRelay relay, Replicator replicator,
                          ReplicaStateRepository states, ObjectMapper objectMapper,
                          @Value("${aisafe.events.enabled:false}") boolean enabled,
                          @Value("${aisafe.node.id:local}") String nodeId,
                          @Value("${spring.application.name}") String service) {
        this(outbox, relay, replicator, states, objectMapper, enabled, nodeId, service, Clock.systemUTC());
    }

    EventPublisher(EventOutboxRepository outbox, EventRelay relay, Replicator replicator, ReplicaStateRepository states,
                   ObjectMapper objectMapper, boolean enabled, String nodeId, String service, Clock clock) {
        this.outbox = outbox;
        this.relay = relay;
        this.replicator = replicator;
        this.states = states;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.nodeId = nodeId;
        this.exchange = Messaging.eventsExchange(service);
        this.clock = clock;
    }

    /**
     * @param replicaType replication type of the entity ({@code airports}, {@code routes}...), to
     *                    find the stamp of the change
     * @param entity      {@code airport}, {@code route}...
     * @param change      {@code created}, {@code updated} or {@code deleted}
     * @param key         the entity's key (the same the replication uses)
     * @param status      status after the change, null when deleted
     * @param version     version after the change (the one its ETag is built from), if any
     * @param data        the entity after the change, null when deleted
     */
    public void changed(String replicaType, String entity, String change, String key, Enum<?> status,
                        Long version, Object data) {
        if (!enabled || replicator.isApplyingReplica()) {
            return;
        }
        // Same id as the Replicator's state of the entity; without one (no peers), stamped now, here
        Optional<ReplicaState> state = states.findById(replicaType + ":" + key);
        String type = entity + "." + change;
        ChangeEvent event = new ChangeEvent(type, key, status == null ? null : status.name(), version, data,
                state.map(ReplicaState::getStamp).orElseGet(clock::millis),
                state.map(ReplicaState::getStampNode).orElse(nodeId),
                nodeId, Instant.now(clock));
        outbox.save(new EventOutboxEntry(exchange, type, objectMapper.writeValueAsString(event), MDC.get("correlationId")));
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            relay.relaySoon();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                relay.relaySoon();
            }
        });
    }
}
