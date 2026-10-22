package aisafe.routes.infrastructure.readmodel;

import aisafe.routes.infrastructure.replication.RouteReplica;
import aisafe.routes.infrastructure.replication.RouteReplicaHandler;
import aisafe.shared.infrastructure.peers.PeerClient;
import aisafe.shared.infrastructure.replication.ReplicaEnvelope;
import aisafe.shared.infrastructure.replication.ReplicaExporter;
import aisafe.shared.infrastructure.security.ServiceTokens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Brings the read model up to date with what the replicas hold (the write model), routes and
 * deletions alike, with the stamp of their last change. Events keep the read model current; this
 * repairs what events cannot:
 *
 * <ul>
 *   <li><b>on startup</b>, before the replica reports ready: the routes created before this
 *   replica's queue existed, or while the broker was down;</li>
 *   <li><b>every {@code aisafe.read-model.reconcile-interval-ms}</b> (60 s by default): an event
 *   lost because the broker could not be reached when it was published.</li>
 * </ul>
 *
 * <p>Peers that do not answer are skipped: their routes are also held by the next replica, or
 * come in the next round.</p>
 */
@Component
@ConditionalOnProperty(name = "aisafe.events.enabled", havingValue = "true")
public class RouteViewReconciler implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RouteViewReconciler.class);

    private final ReplicaExporter exporter;
    private final RouteReplicaHandler handler;
    private final PeerClient peerClient;
    private final ServiceTokens serviceTokens;
    private final RouteViewStore store;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate readOnly;

    public RouteViewReconciler(ReplicaExporter exporter, RouteReplicaHandler handler, PeerClient peerClient,
                               ServiceTokens serviceTokens, RouteViewStore store, ObjectMapper objectMapper,
                               PlatformTransactionManager transactions) {
        this.exporter = exporter;
        this.handler = handler;
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
        // In a transaction: exporting a route also reads its history
        List<ReplicaEnvelope> envelopes = new ArrayList<>(readOnly.execute(status -> exporter.export(handler, null)));
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceTokens.current());
        JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, ReplicaEnvelope.class);
        for (PeerClient.PeerResponse response : peerClient.getFromAll("/internal/replicas/" + handler.type(), headers)) {
            if (!response.found()) {
                continue;
            }
            try {
                envelopes.addAll(objectMapper.readValue(response.body(), listType));
            } catch (RuntimeException e) {
                log.warn("Read model: unreadable routes from {}, skipped: {}", response.peer(), e.getMessage());
            }
        }
        int applied = 0;
        for (ReplicaEnvelope envelope : envelopes) {
            RouteView view = envelope.deleted() ? null
                    : RouteView.from(objectMapper.treeToValue(envelope.payload(), RouteReplica.class).toRoute());
            if (store.apply(envelope.key(), view, envelope.version(),
                    envelope.stamp() == null ? 0 : envelope.stamp(), envelope.stampNode())) {
                applied++;
            }
        }
        if (applied > 0) {
            log.info("Read model: {} of {} route changes from the replicas applied", applied, envelopes.size());
        }
    }
}
