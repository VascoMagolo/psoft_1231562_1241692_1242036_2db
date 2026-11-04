package aisafe.shared.infrastructure.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitOperations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EventRelayTest {

    private final EventOutboxRepository outbox = mock(EventOutboxRepository.class);
    private final RabbitTemplate rabbit = mock(RabbitTemplate.class);
    private final RabbitOperations operations = mock(RabbitOperations.class);
    private final List<EventOutboxEntry> waiting = new ArrayList<>();

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        when(rabbit.invoke(any(RabbitOperations.OperationsCallback.class)))
                .thenAnswer(i -> i.<RabbitOperations.OperationsCallback<?>>getArgument(0).doInRabbit(operations));
        when(outbox.findTop100ByOrderByIdAsc()).thenAnswer(i -> List.copyOf(waiting));
        doAnswer(i -> waiting.remove(i.<EventOutboxEntry>getArgument(0))).when(outbox).delete(any());
    }

    private EventOutboxEntry entry(String routingKey) {
        EventOutboxEntry entry = new EventOutboxEntry("aircraft-maintenance.events", routingKey,
                "{\"type\":\"" + routingKey + "\"}", "c-" + routingKey);
        waiting.add(entry);
        return entry;
    }

    @Test
    void ensureEventsAreSentInOrderConfirmedAndThenRemoved() {
        EventOutboxEntry first = entry("aircraft.created");
        EventOutboxEntry second = entry("aircraft.updated");

        new EventRelay(outbox, rabbit, true).relay();

        InOrder order = inOrder(operations, outbox);
        order.verify(operations).send(eq("aircraft-maintenance.events"), eq("aircraft.created"), any(Message.class));
        order.verify(operations).waitForConfirmsOrDie(anyLong());
        order.verify(outbox).delete(first);
        order.verify(operations).send(eq("aircraft-maintenance.events"), eq("aircraft.updated"), any(Message.class));
        order.verify(outbox).delete(second);
        assertTrue(waiting.isEmpty());
    }

    @Test
    void ensureMessageCarriesTheBodyAndTheCorrelationIdAndIsPersistent() {
        entry("aircraft.created");

        new EventRelay(outbox, rabbit, true).relay();

        ArgumentCaptor<Message> message = ArgumentCaptor.forClass(Message.class);
        verify(operations).send(anyString(), anyString(), message.capture());
        assertEquals("{\"type\":\"aircraft.created\"}", new String(message.getValue().getBody(), StandardCharsets.UTF_8));
        assertEquals("c-aircraft.created", message.getValue().getMessageProperties().getHeader(Messaging.CORRELATION_HEADER));
        assertEquals("application/json", message.getValue().getMessageProperties().getContentType());
        assertEquals(org.springframework.amqp.core.MessageDeliveryMode.PERSISTENT,
                message.getValue().getMessageProperties().getDeliveryMode());
    }

    @Test
    void ensureFirstFailureStopsTheRoundAndKeepsTheEventsForTheNextOne() {
        EventOutboxEntry first = entry("aircraft.created");
        entry("aircraft.updated");
        doThrow(new AmqpConnectException(new ConnectException("refused")))
                .when(operations).send(anyString(), eq("aircraft.created"), any(Message.class));

        new EventRelay(outbox, rabbit, true).relay();

        verify(operations, never()).send(anyString(), eq("aircraft.updated"), any(Message.class));
        verify(outbox, never()).delete(any());
        verify(outbox).save(first);
        assertEquals(1, first.getAttempts());
        assertEquals(2, waiting.size());
    }

    @Test
    void ensureUnconfirmedEventIsKept() {
        entry("aircraft.created");
        doThrow(new AmqpConnectException(new ConnectException("no confirm"))).when(operations).waitForConfirmsOrDie(anyLong());

        new EventRelay(outbox, rabbit, true).relay();

        verify(outbox, never()).delete(any());
        assertEquals(1, waiting.size());
    }

    @Test
    void ensureScheduledRoundDoesNothingWhenEventsAreOff() {
        entry("aircraft.created");

        new EventRelay(outbox, rabbit, false).scheduled();

        verifyNoInteractions(rabbit);
    }
}
