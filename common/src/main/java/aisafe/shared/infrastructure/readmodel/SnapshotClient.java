package aisafe.shared.infrastructure.readmodel;

import aisafe.shared.infrastructure.messaging.CommandBus;
import aisafe.shared.infrastructure.messaging.CommandReply;
import aisafe.shared.infrastructure.messaging.CommandUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fills this replica's copy of other services' entities ({@link ExternalProjection}) by asking the
 * owner for all of them (command {@value SnapshotHandler#TYPE}), and applies each one with the
 * same rule as the events (the later change wins), so a snapshot and the events can arrive in any
 * order. The events keep the copy current; a snapshot brings what they cannot:
 *
 * <ul>
 *   <li><b>on startup</b>, before the replica reports ready: what changed before this replica's
 *   queue existed (a new replica, or the first deployment);</li>
 *   <li><b>every {@code aisafe.read-model.snapshot-interval-ms}</b> (5 min by default), as a check;
 *   and every 15 s while the owner has not answered yet (e.g. it starts after this service).</li>
 * </ul>
 *
 * <p>If the owner does not answer, the copy stays as it is (the reads keep working, maybe behind)
 * and it is asked again on the next round.</p>
 */
@Component
@ConditionalOnProperty(name = "aisafe.events.enabled", havingValue = "true")
public class SnapshotClient implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SnapshotClient.class);

    private final List<ExternalProjection> projections;
    private final CommandBus commands;
    private final ReadModelStore store;
    private final ObjectMapper objectMapper;
    private final long intervalMs;
    private final Clock clock;
    /** When each entity was last filled; absent until its owner first answers. */
    private final Map<String, Long> lastFilled = new ConcurrentHashMap<>();

    @Autowired
    public SnapshotClient(ObjectProvider<ExternalProjection> projections, CommandBus commands, ReadModelStore store,
                          ObjectMapper objectMapper,
                          @Value("${aisafe.read-model.snapshot-interval-ms:300000}") long intervalMs) {
        this(projections.orderedStream().toList(), commands, store, objectMapper, intervalMs, Clock.systemUTC());
    }

    SnapshotClient(List<ExternalProjection> projections, CommandBus commands, ReadModelStore store,
                   ObjectMapper objectMapper, long intervalMs, Clock clock) {
        this.projections = projections;
        this.commands = commands;
        this.store = store;
        this.objectMapper = objectMapper;
        this.intervalMs = intervalMs;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) {
        projections.forEach(this::fill);
    }

    /** Every 15 s: the entities never filled yet, and those filled longer than the interval ago. */
    @Scheduled(initialDelay = 15_000, fixedDelay = 15_000)
    public void refresh() {
        long now = clock.millis();
        projections.stream()
                .filter(p -> {
                    Long last = lastFilled.get(key(p));
                    return last == null || now - last >= intervalMs;
                })
                .forEach(this::fill);
    }

    void fill(ExternalProjection projection) {
        CommandReply reply;
        try {
            reply = commands.send(projection.source(), SnapshotHandler.TYPE, Map.of("entity", projection.entity()));
        } catch (CommandUnavailableException e) {
            log.warn("Read model: no snapshot of {} from {} yet ({}); asked again later", projection.entity(),
                    projection.source(), e.getMessage());
            return;
        }
        if (!reply.ok() || reply.body() == null) {
            log.warn("Read model: snapshot of {} refused by {}: {} {}", projection.entity(), projection.source(),
                    reply.status(), reply.error());
            return;
        }
        int applied = 0;
        int total = 0;
        for (JsonNode node : reply.body()) {
            total++;
            try {
                ReadModelStore.StampedRow row = objectMapper.treeToValue(node, ReadModelStore.StampedRow.class);
                if (store.apply(projection.entity(), row.key(), row.view(), row.version(), row.stamp(), row.stampNode())) {
                    applied++;
                }
            } catch (RuntimeException e) {
                log.warn("Read model: {} row of the snapshot not applied: {}", projection.entity(), e.toString());
            }
        }
        // An empty answer is not taken as filled: during a deploy the owner's replica that answers may
        // not have filled its own read model yet, so it is asked again in 15 s rather than in 5 min
        if (total > 0) {
            lastFilled.put(key(projection), clock.millis());
        }
        if (applied > 0) {
            log.info("Read model: {} of {} {} from {} applied by the snapshot", applied, total, projection.entity(),
                    projection.source());
        }
    }

    private static String key(ExternalProjection projection) {
        return projection.source() + "/" + projection.entity();
    }
}
