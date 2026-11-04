package aisafe.security.infrastructure.persistence.jpa;

import aisafe.security.domain.Role;
import aisafe.security.domain.User;
import aisafe.security.domain.UserRepository;
import aisafe.shared.infrastructure.messaging.EventOutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitOperations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** An account created or changed is published on {@code login.events}, with its role but never its password hash. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:login-events-it",
        "aisafe.events.enabled=true",
        "spring.rabbitmq.listener.simple.auto-startup=false"
})
@ActiveProfiles("jpa")
class AccountEventsTest {

    @MockitoBean private RabbitTemplate rabbit;
    private final RabbitOperations operations = mock(RabbitOperations.class);

    @Autowired private UserRepository users;
    @Autowired private EventOutboxRepository outbox;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PlatformTransactionManager transactions;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void captureWhatTheRelaySends() {
        when(rabbit.invoke(any(RabbitOperations.OperationsCallback.class)))
                .thenAnswer(i -> i.<RabbitOperations.OperationsCallback<?>>getArgument(0).doInRabbit(operations));
    }

    @Test
    void ensureNewAccountIsPublishedWithoutItsPassword() {
        new TransactionTemplate(transactions).executeWithoutResult(status ->
                users.save(new User("events-user", "$2a$10$secret-hash", Role.ATCC)));

        // The relay sends from its own thread, after the accounts the login creates on startup
        long deadline = System.currentTimeMillis() + 5_000;
        while (outbox.count() > 0 && System.currentTimeMillis() < deadline) {
            Thread.onSpinWait();
        }
        assertEquals(0, outbox.count(), "every event was sent");
        ArgumentCaptor<Message> sent = ArgumentCaptor.forClass(Message.class);
        verify(operations, atLeastOnce()).send(eq("login.events"), eq("account.created"), sent.capture());
        Message message = sent.getAllValues().stream()
                .filter(m -> new String(m.getBody()).contains("\"key\":\"events-user\"")).findFirst().orElseThrow();
        JsonNode event = objectMapper.readTree(message.getBody());
        assertEquals("events-user", event.get("key").asString());
        assertEquals("ATCC", event.get("data").get("role").asString());
        assertFalse(new String(message.getBody()).contains("secret-hash"));
    }
}
