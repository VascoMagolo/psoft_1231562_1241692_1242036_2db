package aisafe.shared.infrastructure.messaging;

import aisafe.shared.domain.ConcurrencyException;
import aisafe.shared.domain.DomainException;
import aisafe.shared.domain.NotFoundException;
import aisafe.shared.domain.ResourceInUseException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataIntegrityViolationException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.ConnectException;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** The command bus (sender) and the dispatcher (receiver), each on its own. */
class CommandsTest {

    private final JsonMapper json = JsonMapper.builder().build();

    private static CommandHandler handler(String type, java.util.function.Function<JsonNode, Object> body) {
        return new CommandHandler() {
            @Override public String type() { return type; }
            @Override public Object handle(JsonNode payload) { return body.apply(payload); }
        };
    }

    @SuppressWarnings("unchecked")
    private CommandDispatcher dispatcher(CommandHandler... handlers) {
        ObjectProvider<CommandHandler> provider = mock(ObjectProvider.class);
        when(provider.orderedStream()).thenAnswer(i -> Stream.of(handlers));
        return new CommandDispatcher(provider, json, "aircraft-maintenance-1");
    }

    private Message command(String type, Object payload) {
        MessageProperties properties = new MessageProperties();
        properties.setHeader(Messaging.COMMAND_TYPE_HEADER, type);
        properties.setHeader(Messaging.SENDER_HEADER, "flight-ops");
        properties.setHeader(Messaging.CORRELATION_HEADER, "c-9");
        return new Message(json.writeValueAsBytes(payload), properties);
    }

    private CommandReply replyOf(Message answer) {
        return json.readValue(answer.getBody(), CommandReply.class);
    }

    @Test
    void ensureHandlerResultIsAnsweredWith200() {
        CommandDispatcher dispatcher = dispatcher(handler("echo", p -> Map.of("id", p.get("id").asString())));

        CommandReply reply = replyOf(dispatcher.onCommand(command("echo", Map.of("id", "F-1"))));

        assertEquals(200, reply.status());
        assertTrue(reply.ok());
        assertEquals("F-1", reply.body().get("id").asString());
        assertNull(reply.error());
    }

    @Test
    void ensureDomainExceptionsGetTheStatusOfTheHttpRoutes() {
        assertEquals(404, CommandDispatcher.statusOf(new NotFoundException("no aircraft")));
        assertEquals(409, CommandDispatcher.statusOf(new ResourceInUseException("in maintenance")));
        assertEquals(409, CommandDispatcher.statusOf(new DataIntegrityViolationException("duplicate")));
        assertEquals(412, CommandDispatcher.statusOf(new ConcurrencyException("stale")));
        assertEquals(400, CommandDispatcher.statusOf(new DomainException("invalid")));
        assertEquals(400, CommandDispatcher.statusOf(new IllegalArgumentException("invalid")));
        assertEquals(500, CommandDispatcher.statusOf(new IllegalStateException("bug")));
    }

    @Test
    void ensureFailedCommandIsAnsweredWithItsStatusAndReason() {
        CommandDispatcher dispatcher = dispatcher(handler("reserve", p -> {
            throw new ResourceInUseException("Aircraft CS-TKA cannot take a new flight");
        }));

        CommandReply reply = replyOf(dispatcher.onCommand(command("reserve", Map.of())));

        assertEquals(409, reply.status());
        assertFalse(reply.ok());
        assertEquals("Aircraft CS-TKA cannot take a new flight", reply.error());
    }

    @Test
    void ensureUnknownCommandIsRefusedWith400() {
        CommandReply reply = replyOf(dispatcher().onCommand(command("nothing", Map.of())));

        assertEquals(400, reply.status());
    }

    @Test
    void ensureBusSendsToTheServiceWithTypeSenderCorrelationAndExpiry() {
        RabbitTemplate rabbit = mock(RabbitTemplate.class);
        MessageProperties answer = new MessageProperties();
        when(rabbit.sendAndReceive(eq(Messaging.COMMANDS_EXCHANGE), eq("aircraft-maintenance"), any(Message.class)))
                .thenReturn(new Message(json.writeValueAsBytes(new CommandReply(409, null, "busy")), answer));
        MDC.put("correlationId", "c-7");
        try {
            CommandReply reply = new CommandBus(rabbit, json, "flight-ops", 5_000)
                    .send("aircraft-maintenance", "flightcopy.record", Map.of("id", "F-1"));

            assertEquals(409, reply.status());
            assertEquals("busy", reply.error());
        } finally {
            MDC.clear();
        }
        ArgumentCaptor<Message> sent = ArgumentCaptor.forClass(Message.class);
        verify(rabbit).sendAndReceive(eq(Messaging.COMMANDS_EXCHANGE), eq("aircraft-maintenance"), sent.capture());
        MessageProperties properties = sent.getValue().getMessageProperties();
        assertEquals("flightcopy.record", properties.getHeader(Messaging.COMMAND_TYPE_HEADER));
        assertEquals("flight-ops", properties.getHeader(Messaging.SENDER_HEADER));
        assertEquals("c-7", properties.getHeader(Messaging.CORRELATION_HEADER));
        assertEquals("5000", properties.getExpiration());
        assertEquals("F-1", json.readTree(sent.getValue().getBody()).get("id").asString());
    }

    @Test
    void ensureNoAnswerInTimeIsUnavailable() {
        RabbitTemplate rabbit = mock(RabbitTemplate.class);
        when(rabbit.sendAndReceive(any(String.class), any(String.class), any(Message.class))).thenReturn(null);

        assertThrows(CommandUnavailableException.class,
                () -> new CommandBus(rabbit, json, "flight-ops", 5_000).send("aircraft-maintenance", "x", Map.of()));
    }

    @Test
    void ensureBrokerDownIsUnavailable() {
        RabbitTemplate rabbit = mock(RabbitTemplate.class);
        when(rabbit.sendAndReceive(any(String.class), any(String.class), any(Message.class)))
                .thenThrow(new AmqpConnectException(new ConnectException("refused")));

        assertThrows(CommandUnavailableException.class,
                () -> new CommandBus(rabbit, json, "flight-ops", 5_000).send("aircraft-maintenance", "x", Map.of()));
    }

    @Test
    void ensureIdempotentCommandIsSentOnceMoreWhenUnanswered() {
        RabbitTemplate rabbit = mock(RabbitTemplate.class);
        when(rabbit.sendAndReceive(any(String.class), any(String.class), any(Message.class)))
                .thenReturn(null)
                .thenReturn(new Message(json.writeValueAsBytes(new CommandReply(200, null, null)), new MessageProperties()));

        CommandReply reply = new CommandBus(rabbit, json, "flight-ops", 5_000).sendIdempotent("aircraft-maintenance", "x", Map.of());

        assertEquals(200, reply.status());
        verify(rabbit, times(2)).sendAndReceive(any(String.class), any(String.class), any(Message.class));
    }

    @Test
    void ensureIdempotentCommandUnansweredTwiceIsUnavailable() {
        RabbitTemplate rabbit = mock(RabbitTemplate.class);
        when(rabbit.sendAndReceive(any(String.class), any(String.class), any(Message.class))).thenReturn(null);

        assertThrows(CommandUnavailableException.class,
                () -> new CommandBus(rabbit, json, "flight-ops", 5_000).sendIdempotent("aircraft-maintenance", "x", Map.of()));
    }
}
