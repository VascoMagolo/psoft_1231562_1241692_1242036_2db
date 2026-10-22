package aisafe.airportsroutes.infrastructure.messaging;

import aisafe.routes.domain.RouteStatus;
import aisafe.routes.infrastructure.readmodel.RouteView;
import aisafe.shared.infrastructure.replication.ReplicaState;
import aisafe.shared.infrastructure.replication.ReplicaStateRepository;
import aisafe.shared.infrastructure.replication.Replicator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.ConnectException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EventPublisherTest {

    private final RabbitTemplate rabbit = mock(RabbitTemplate.class);
    private final Replicator replicator = mock(Replicator.class);
    private final ReplicaStateRepository states = mock(ReplicaStateRepository.class);
    private final JsonMapper json = JsonMapper.builder().build();
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-01T10:00:00Z"), ZoneOffset.UTC);

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private EventPublisher publisher(boolean enabled) {
        return new EventPublisher(rabbit, replicator, states, json, enabled, "airports-routes-0", clock);
    }

    @Test
    void publishesTheChangeToTheExchangeWithItsRoutingKey() {
        RouteView view = new RouteView("OPO", "LIS", 55, 300.0, 100, RouteStatus.ACTIVE);
        publisher(true).changed("routes", "route", "created", "OPO-LIS", RouteStatus.ACTIVE, 3L, view);

        ArgumentCaptor<Message> message = ArgumentCaptor.forClass(Message.class);
        verify(rabbit).send(eq(EventPublisher.EXCHANGE), eq("route.created"), message.capture());
        JsonNode body = json.readTree(message.getValue().getBody());
        assertEquals("route.created", body.get("type").asString());
        assertEquals("OPO-LIS", body.get("key").asString());
        assertEquals("ACTIVE", body.get("status").asString());
        assertEquals("airports-routes-0", body.get("nodeId").asString());
        assertEquals(3, body.get("version").asLong());
        assertEquals(55, body.get("data").get("estimatedFlightTime").asInt());
        // No replication state (no peers): stamped now, by this replica
        assertEquals(clock.millis(), body.get("stamp").asLong());
        assertEquals("airports-routes-0", body.get("stampNode").asString());
        assertEquals("application/json", message.getValue().getMessageProperties().getContentType());
    }

    @Test
    void carriesTheStampTheReplicatorGaveTheChange() {
        ReplicaState state = mock(ReplicaState.class);
        when(state.getStamp()).thenReturn(1_000L);
        when(state.getStampNode()).thenReturn("airports-routes-0");
        when(states.findById("routes:OPO-LIS")).thenReturn(Optional.of(state));

        publisher(true).changed("routes", "route", "updated", "OPO-LIS", RouteStatus.ACTIVE, 4L, null);

        ArgumentCaptor<Message> message = ArgumentCaptor.forClass(Message.class);
        verify(rabbit).send(eq(EventPublisher.EXCHANGE), eq("route.updated"), message.capture());
        assertEquals(1_000L, json.readTree(message.getValue().getBody()).get("stamp").asLong());
    }

    @Test
    void deletedHasNoStatus() {
        publisher(true).changed("airports", "airport", "deleted", "FAO", null, null, null);

        ArgumentCaptor<Message> message = ArgumentCaptor.forClass(Message.class);
        verify(rabbit).send(eq(EventPublisher.EXCHANGE), eq("airport.deleted"), message.capture());
        assertTrue(json.readTree(message.getValue().getBody()).get("status").isNull());
    }

    @Test
    void offByDefaultSendsNothing() {
        publisher(false).changed("routes", "route", "created", "OPO-LIS", RouteStatus.ACTIVE, 0L, null);

        verifyNoInteractions(rabbit);
    }

    @Test
    void aCopyReceivedFromAPeerIsNotPublishedAgain() {
        when(replicator.isApplyingReplica()).thenReturn(true);

        publisher(true).changed("routes", "route", "updated", "OPO-LIS", RouteStatus.INACTIVE, 1L, null);

        verifyNoInteractions(rabbit);
    }

    @Test
    void inATransactionWaitsForTheCommit() {
        TransactionSynchronizationManager.initSynchronization();

        publisher(true).changed("routes", "route", "created", "OPO-LIS", RouteStatus.ACTIVE, 0L, null);
        verifyNoInteractions(rabbit);

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        verify(rabbit).send(eq(EventPublisher.EXCHANGE), eq("route.created"), any(Message.class));
    }

    @Test
    void brokerDownDoesNotFailTheChange() {
        doThrow(new AmqpConnectException(new ConnectException("refused")))
                .when(rabbit).send(anyString(), anyString(), any(Message.class));

        assertDoesNotThrow(() -> publisher(true).changed("routes", "route", "created", "OPO-LIS", RouteStatus.ACTIVE, 0L, null));
    }
}
