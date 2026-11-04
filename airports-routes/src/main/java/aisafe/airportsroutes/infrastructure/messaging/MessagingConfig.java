package aisafe.airportsroutes.infrastructure.messaging;

import aisafe.airportsroutes.infrastructure.readmodel.Projection;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Declares on RabbitMQ what this service uses, only when events are on: the exchange where it
 * publishes its events, and this replica's queue for the read model, bound to the events of every
 * entity the read model keeps ({@code route.*}, {@code airport.*}, {@code flightcopy.*}). All
 * durable, so they survive a restart of the broker, and the queue keeps the events that arrive
 * while this replica is down.
 */
@Configuration
@ConditionalOnProperty(name = "aisafe.events.enabled", havingValue = "true")
public class MessagingConfig {

    @Bean
    TopicExchange airportsRoutesEvents() {
        return new TopicExchange(EventPublisher.EXCHANGE, true, false);
    }

    /** One queue per replica (publish/subscribe): every replica gets every event. */
    @Bean
    Queue readModelQueue(@Value("${aisafe.node.id:local}") String nodeId) {
        return new Queue("airports-routes.read-model." + nodeId, true);
    }

    @Bean
    Declarables readModelBindings(Queue readModelQueue, TopicExchange airportsRoutesEvents, List<Projection> projections) {
        return new Declarables(projections.stream()
                .map(p -> BindingBuilder.bind(readModelQueue).to(airportsRoutesEvents).with(p.entity() + ".*"))
                .toList());
    }
}
