package aisafe.shared.infrastructure.messaging;

import aisafe.shared.infrastructure.replication.ReplicaState;
import aisafe.shared.infrastructure.replication.ReplicaStateRepository;
import aisafe.shared.infrastructure.replication.Replicator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EventPublisherTest {

    enum Status { ACTIVE }

    private final EventOutboxRepository outbox = mock(EventOutboxRepository.class);
    private final EventRelay relay = mock(EventRelay.class);
    private final Replicator replicator = mock(Replicator.class);
    private final ReplicaStateRepository states = mock(ReplicaStateRepository.class);
    private final JsonMapper json = JsonMapper.builder().build();
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-01T10:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        when(outbox.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        MDC.clear();
    }

    private EventPublisher publisher(boolean enabled) {
        return new EventPublisher(outbox, relay, replicator, states, json, enabled, "airports-routes-0", "airports-routes", clock);
    }

    private EventOutboxEntry written() {
        ArgumentCaptor<EventOutboxEntry> entry = ArgumentCaptor.forClass(EventOutboxEntry.class);
        verify(outbox).save(entry.capture());
        return entry.getValue();
    }

    @Test
    void ensureChangeIsWrittenToTheOutboxForTheServiceExchange() {
        MDC.put("correlationId", "c-1");
        publisher(true).changed("routes", "route", "created", "OPO-LIS", Status.ACTIVE, 3L,
                Map.of("estimatedFlightTime", 55));

        EventOutboxEntry entry = written();
        assertEquals("airports-routes.events", entry.getExchange());
        assertEquals("route.created", entry.getRoutingKey());
        assertEquals("c-1", entry.getCorrelationId());
        JsonNode body = json.readTree(entry.getBody());
        assertEquals("route.created", body.get("type").asString());
        assertEquals("OPO-LIS", body.get("key").asString());
        assertEquals("ACTIVE", body.get("status").asString());
        assertEquals("airports-routes-0", body.get("nodeId").asString());
        assertEquals(3, body.get("version").asLong());
        assertEquals(55, body.get("data").get("estimatedFlightTime").asInt());
        // No replication state (no peers): stamped now, by this replica
        assertEquals(clock.millis(), body.get("stamp").asLong());
        assertEquals("airports-routes-0", body.get("stampNode").asString());
    }

    @Test
    void ensureEventCarriesTheStampTheReplicatorGaveTheChange() {
        ReplicaState state = mock(ReplicaState.class);
        when(state.getStamp()).thenReturn(1_000L);
        when(state.getStampNode()).thenReturn("airports-routes-2");
        when(states.findById("routes:OPO-LIS")).thenReturn(Optional.of(state));

        publisher(true).changed("routes", "route", "updated", "OPO-LIS", Status.ACTIVE, 4L, null);

        JsonNode body = json.readTree(written().getBody());
        assertEquals(1_000L, body.get("stamp").asLong());
        assertEquals("airports-routes-2", body.get("stampNode").asString());
    }

    @Test
    void ensureDeletionHasNoStatusNorData() {
        publisher(true).changed("airports", "airport", "deleted", "FAO", null, null, null);

        JsonNode body = json.readTree(written().getBody());
        assertTrue(body.get("status").isNull());
        assertTrue(body.get("data").isNull());
    }

    @Test
    void ensureNothingIsWrittenWhenEventsAreOff() {
        publisher(false).changed("routes", "route", "created", "OPO-LIS", Status.ACTIVE, 0L, null);

        verifyNoInteractions(outbox, relay);
    }

    @Test
    void ensureCopyReceivedFromAPeerIsNotPublishedAgain() {
        when(replicator.isApplyingReplica()).thenReturn(true);

        publisher(true).changed("routes", "route", "updated", "OPO-LIS", Status.ACTIVE, 1L, null);

        verifyNoInteractions(outbox, relay);
    }

    @Test
    void ensureInATransactionTheRelayStartsAfterTheCommit() {
        TransactionSynchronizationManager.initSynchronization();

        publisher(true).changed("routes", "route", "created", "OPO-LIS", Status.ACTIVE, 0L, null);
        verify(outbox).save(any());
        verifyNoInteractions(relay);

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        verify(relay).relaySoon();
    }

    @Test
    void ensureWithoutATransactionTheRelayStartsAtOnce() {
        publisher(true).changed("routes", "route", "created", "OPO-LIS", Status.ACTIVE, 0L, null);

        verify(relay).relaySoon();
    }
}
