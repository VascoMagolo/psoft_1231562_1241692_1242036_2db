package aisafe.routes.infrastructure.readmodel;

import aisafe.airportsroutes.infrastructure.messaging.EventPublisher;
import aisafe.routes.application.RoutesAcrossReplicas;
import aisafe.airports.domain.IataCode;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteHistory;
import aisafe.routes.domain.RouteHistoryRepository;
import aisafe.routes.domain.RouteRepository;
import aisafe.routes.domain.RouteStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Events on, without a broker: what would be sent to RabbitMQ is captured and handed to the
 * listener by hand. This replica is airports-routes-0 in a ring of three (the peers do not exist).
 * Every write in its own transaction, as the use cases do: the events leave after the commit.
 */
@SpringBootTest(properties = {
        "aisafe.events.enabled=true",
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "aisafe.node.id=airports-routes-0",
        "aisafe.peers=http://airports-routes-0.test.invalid:1,http://airports-routes-1.test.invalid:1,http://airports-routes-2.test.invalid:1",
        "aisafe.replication.dispatch-interval-ms=3600000",
        "aisafe.read-model.reconcile-interval-ms=3600000"
})
@ActiveProfiles("jpa")
class ReadModelIntegrationTest {

    @MockitoBean private RabbitTemplate rabbit;

    @Autowired private RouteRepository routeRepository;
    @Autowired private RouteHistoryRepository historyRepository;
    @Autowired private RoutesAcrossReplicas routesAcrossReplicas;
    @Autowired private RouteEventListener listener;
    @Autowired private RouteViewReconciler reconciler;
    @Autowired private PlatformTransactionManager transactions;

    private void inTransaction(Runnable action) {
        new TransactionTemplate(transactions).executeWithoutResult(status -> action.run());
    }

    @AfterEach
    void tearDown() {
        inTransaction(() -> routeRepository.findAll().stream()
                .filter(r -> r.getOrigin().getCode().equals("FAO"))
                .forEach(r -> {
                    historyRepository.deleteAllByRoute("FAO", r.getDestination().getCode());
                    routeRepository.delete(r);
                }));
    }

    private static List<String> keys(List<Route> routes) {
        return routes.stream().map(r -> r.getOrigin().getCode() + "-" + r.getDestination().getCode()).sorted().toList();
    }

    private List<String> readModelKeys() {
        return keys(routesAcrossReplicas.findAll().stream().map(v -> v.value()).toList());
    }

    private Message lastEvent(String routingKey) {
        ArgumentCaptor<Message> message = ArgumentCaptor.forClass(Message.class);
        verify(rabbit, org.mockito.Mockito.atLeastOnce()).send(eq(EventPublisher.EXCHANGE), eq(routingKey), message.capture());
        return message.getValue();
    }

    @Test
    void ensureListingsReadTheReadModel() {
        assertInstanceOf(ReadModelRoutes.class, routesAcrossReplicas);
    }

    @Test
    void ensureReconciliationFillsTheReadModelWithEveryRouteHeldHere() {
        // A route with history: exporting it reads the history, outside any request or transaction
        inTransaction(() -> {
            routeRepository.save(new Route("FAO", "OPO", 50, 300.0, 80));
            historyRepository.save(new RouteHistory(new IataCode("FAO"), new IataCode("OPO"), "created",
                    LocalDateTime.of(2026, 1, 1, 10, 0), "atcc"));
        });

        reconciler.reconcile();

        assertEquals(keys(routeRepository.findAll()), readModelKeys());
    }

    @Test
    void ensureRouteEventsReachTheReadModel() {
        inTransaction(() -> routeRepository.save(new Route("FAO", "LIS", 40, 250.0, 80)));
        listener.onRouteEvent(lastEvent("route.created"));
        assertTrue(readModelKeys().contains("FAO-LIS"));

        Route changed = routeRepository.findByOriginAndDestination(new IataCode("FAO"), new IataCode("LIS")).orElseThrow();
        changed.changeStatus(RouteStatus.INACTIVE);
        inTransaction(() -> routeRepository.save(changed));
        listener.onRouteEvent(lastEvent("route.updated"));
        Optional<Route> view = routesAcrossReplicas.findAll().stream().map(v -> v.value())
                .filter(r -> r.getOrigin().getCode().equals("FAO")).findFirst();
        assertEquals(RouteStatus.INACTIVE, view.orElseThrow().getStatus());

        inTransaction(() -> routeRepository.delete(changed));
        listener.onRouteEvent(lastEvent("route.deleted"));
        assertFalse(readModelKeys().contains("FAO-LIS"));
    }
}
