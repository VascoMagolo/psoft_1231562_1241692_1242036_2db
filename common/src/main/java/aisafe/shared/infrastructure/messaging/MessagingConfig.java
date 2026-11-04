package aisafe.shared.infrastructure.messaging;

import aisafe.shared.infrastructure.readmodel.ExternalProjection;
import aisafe.shared.infrastructure.readmodel.Projection;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Declares on RabbitMQ what this service uses, only when events are on (see {@link Messaging} for
 * the names). Declaring is idempotent, so every service declares the exchanges it publishes to and
 * reads from, whichever starts first.
 *
 * <ul>
 *   <li>the exchange of its events, and the exchanges of the services it reads;</li>
 *   <li>this replica's read-model queue, bound to {@code <entity>.*} of every entity its read
 *   model keeps, its own ({@link Projection}) and other services' ({@link ExternalProjection});</li>
 *   <li>the service's command queue, shared by its replicas;</li>
 *   <li>the dead-letter exchange and the service's dead-letter queue: an event that cannot be
 *   applied, after {@value #DELIVERY_LIMIT} attempts, or a command that expired before anyone took it.</li>
 * </ul>
 *
 * <p>Every queue is a quorum queue: durable, and replicated on the nodes of the RabbitMQ cluster,
 * so it survives the loss of one of them. A queue keeps what arrives while its replica is down.</p>
 */
@Configuration
@ConditionalOnProperty(name = "aisafe.events.enabled", havingValue = "true")
public class MessagingConfig {

    static final int DELIVERY_LIMIT = 5;

    private final String service;

    public MessagingConfig(@Value("${spring.application.name}") String service) {
        this.service = service;
    }

    @Bean
    TopicExchange eventsExchange() {
        return new TopicExchange(Messaging.eventsExchange(service), true, false);
    }

    @Bean
    DirectExchange commandsExchange() {
        return new DirectExchange(Messaging.COMMANDS_EXCHANGE, true, false);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(Messaging.DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(Messaging.deadLetterQueue(service)).quorum().build();
    }

    @Bean
    Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(service);
    }

    /** One queue per replica (publish/subscribe): every replica gets every event. */
    @Bean
    Queue readModelQueue(@Value("${aisafe.node.id:local}") String nodeId) {
        return withDeadLetter(QueueBuilder.durable(Messaging.readModelQueue(service, nodeId)).quorum()
                .deliveryLimit(DELIVERY_LIMIT)).build();
    }

    /** One queue for the service (competing consumers): each command is taken by one replica. */
    @Bean
    Queue commandQueue() {
        return withDeadLetter(QueueBuilder.durable(Messaging.commandQueue(service)).quorum()
                .deliveryLimit(DELIVERY_LIMIT)).build();
    }

    @Bean
    Binding commandBinding(Queue commandQueue, DirectExchange commandsExchange) {
        return BindingBuilder.bind(commandQueue).to(commandsExchange).with(service);
    }

    @Bean
    Declarables readModelBindings(Queue readModelQueue, TopicExchange eventsExchange,
                                  ObjectProvider<Projection> projections,
                                  ObjectProvider<ExternalProjection> externalProjections) {
        List<Declarable> declarables = new ArrayList<>();
        projections.orderedStream().forEach(p ->
                declarables.add(BindingBuilder.bind(readModelQueue).to(eventsExchange).with(p.entity() + ".*")));
        Set<String> sources = new LinkedHashSet<>();
        externalProjections.orderedStream().forEach(p -> {
            TopicExchange source = new TopicExchange(Messaging.eventsExchange(p.source()), true, false);
            if (sources.add(p.source())) {
                declarables.add(source);
            }
            declarables.add(BindingBuilder.bind(readModelQueue).to(source).with(p.entity() + ".*"));
        });
        return new Declarables(declarables);
    }

    private QueueBuilder withDeadLetter(QueueBuilder builder) {
        return builder.deadLetterExchange(Messaging.DEAD_LETTER_EXCHANGE).deadLetterRoutingKey(service);
    }
}
