package aisafe.airportsroutes.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares on RabbitMQ what this service uses, only when events are on: the exchange where it
 * publishes its events, and this replica's queue for the route read model. All durable, so they
 * survive a restart of the broker, and the queue keeps the events that arrive while this replica
 * is down.
 */
@Configuration
@ConditionalOnProperty(name = "aisafe.events.enabled", havingValue = "true")
public class MessagingConfig {

    @Bean
    TopicExchange airportsRoutesEvents() {
        return new TopicExchange(EventPublisher.EXCHANGE, true, false);
    }

    /** One queue per replica (publish/subscribe): every replica gets every route event. */
    @Bean
    Queue routeReadModelQueue(@Value("${aisafe.node.id:local}") String nodeId) {
        return new Queue("airports-routes.read-model." + nodeId, true);
    }

    @Bean
    Binding routeReadModelBinding(Queue routeReadModelQueue, TopicExchange airportsRoutesEvents) {
        return BindingBuilder.bind(routeReadModelQueue).to(airportsRoutesEvents).with("route.*");
    }
}
