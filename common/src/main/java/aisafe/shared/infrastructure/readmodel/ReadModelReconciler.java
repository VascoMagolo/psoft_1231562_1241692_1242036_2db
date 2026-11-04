package aisafe.shared.infrastructure.readmodel;

import aisafe.shared.infrastructure.peers.PeerClient;
import aisafe.shared.infrastructure.replication.ReplicaEnvelope;
import aisafe.shared.infrastructure.replication.ReplicaExporter;
import aisafe.shared.infrastructure.security.ServiceTokens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Brings the read model up to date with what the replicas hold (the write model), entities and
 * deletions alike, with the stamp of their last change. Events keep the read model current; this
 * repairs what events cannot:
 *
 * <ul>
 *   <li><b>on startup</b>, before the replica reports ready: what was written before this
 *   replica's queue existed, or while the broker was down;</li>
 *   <li><b>every {@code aisafe.read-model.reconcile-interval-ms}</b> (60 s by default): an event
 *   lost because the broker could not be reached when it was published.</li>
 * </ul>
 *
 * <p>Only for the entities of this service ({@link Projection}); those of other services come
 * back with the {@link SnapshotClient}. It reads the same export the replication uses: this
 * replica's own, and for records also the peers' ({@code GET /internal/replicas/{type}}). Catalogs
 * are already on every replica, so this replica's export has them all. Peers that do not answer are
 * skipped: their records are also held by the next replica, or come in the next round. A change
 * that cannot be applied is logged and skipped, so it never stops the round, nor the startup.</p>
 */
@Component
@ConditionalOnProperty(name = "aisafe.events.enabled", havingValue = "true")
public class ReadModelReconciler implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ReadModelReconciler.class);

    private final List<Projection> projections;
    private final ReplicaExporter exporter;
    private final PeerClient peerClient;
    private final ServiceTokens serviceTokens;
    private final ReadModelStore store;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate readOnly;

    public ReadModelReconciler(ObjectProvider<Projection> projections, ReplicaExporter exporter, PeerClient peerClient,
                               ServiceTokens serviceTokens, ReadModelStore store, ObjectMapper objectMapper,
                               PlatformTransactionManager transactions) {
        this.projections = projections.orderedStream().toList();
        this.exporter = exporter;
        this.peerClient = peerClient;
        this.serviceTokens = serviceTokens;
        this.store = store;
        this.objectMapper = objectMapper;
        this.readOnly = new TransactionTemplate(transactions);
        this.readOnly.setReadOnly(true);
    }

    @Override
    public void run(ApplicationArguments args) {
        reconcile();
    }

    @Scheduled(initialDelayString = "${aisafe.read-model.reconcile-interval-ms:60000}",
               fixedDelayString = "${aisafe.read-model.reconcile-interval-ms:60000}")
    public void reconcile() {
        for (Projection projection : projections) {
            try {
                reconcile(projection);
            } catch (RuntimeException e) {
                log.warn("Read model: {} not reconciled this round: {}", projection.entity(), e.toString());
            }
        }
    }

    private void reconcile(Projection projection) {
        // In a transaction: exporting an entity may read related rows (e.g. a route's history)
        List<ReplicaEnvelope> envelopes = new ArrayList<>(readOnly.execute(status -> exporter.export(projection.handler(), null)));
        if (!projection.handler().isCatalog()) {
            envelopes.addAll(fromPeers(projection));
        }
        int applied = 0;
        for (ReplicaEnvelope envelope : envelopes) {
            try {
                Object view = envelope.deleted() ? null : projection.view(envelope.payload());
                if (store.apply(projection.entity(), envelope.key(), view == null ? null : objectMapper.valueToTree(view),
                        envelope.version(), envelope.stamp() == null ? 0 : envelope.stamp(), envelope.stampNode())) {
                    applied++;
                }
            } catch (RuntimeException e) {
                log.warn("Read model: {} {} not applied: {}", projection.entity(), envelope.key(), e.toString());
            }
        }
        if (applied > 0) {
            log.info("Read model: {} of {} {} changes from the replicas applied", applied, envelopes.size(), projection.entity());
        }
    }

    private List<ReplicaEnvelope> fromPeers(Projection projection) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceTokens.current());
        JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, ReplicaEnvelope.class);
        List<ReplicaEnvelope> envelopes = new ArrayList<>();
        for (PeerClient.PeerResponse response : peerClient.getFromAll("/internal/replicas/" + projection.handler().type(), headers)) {
            if (!response.found()) {
                continue;
            }
            try {
                envelopes.addAll(objectMapper.readValue(response.body(), listType));
            } catch (RuntimeException e) {
                log.warn("Read model: unreadable {} from {}, skipped: {}", projection.handler().type(), response.peer(), e.getMessage());
            }
        }
        return envelopes;
    }
}
