package aisafe.airportsroutes.infrastructure.readmodel;

import aisafe.shared.infrastructure.readmodel.ReadModelListener;
import aisafe.shared.infrastructure.readmodel.ReadModelReconciler;
import aisafe.airports.application.AirportListings;
import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.IataCode;
import aisafe.airports.domain.Runway;
import aisafe.airports.infrastructure.readmodel.ReadModelAirportListings;
import aisafe.flightcopies.application.FlightCopiesAcrossReplicas;
import aisafe.flightcopies.domain.FlightCopy;
import aisafe.flightcopies.domain.FlightCopyRepository;
import aisafe.flightcopies.domain.FlightCopyStatus;
import aisafe.flightcopies.infrastructure.readmodel.ReadModelFlightCopies;
import aisafe.routes.application.RoutesAcrossReplicas;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteHistory;
import aisafe.routes.domain.RouteHistoryRepository;
import aisafe.routes.domain.RouteRepository;
import aisafe.routes.domain.RouteStatus;
import aisafe.routes.infrastructure.readmodel.ReadModelRoutes;
import aisafe.shared.application.Versioned;
import org.junit.jupiter.api.AfterEach;
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

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

/**
 * Events on, without a broker: what would be sent to RabbitMQ is captured and handed to the
 * listener by hand. This replica is airports-routes-0 in a ring of three (the peers do not exist).
 * Every write in its own transaction, as the use cases do: the events leave after the commit.
 */
@SpringBootTest(properties = {
        // A database of its own: its deletions leave tombstones that would hide the same keys from other tests
        "spring.datasource.url=jdbc:h2:mem:readmodel-it",
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
    private final RabbitOperations operations = mock(RabbitOperations.class);

    @Autowired private RouteRepository routeRepository;
    @Autowired private RouteHistoryRepository historyRepository;
    @Autowired private AirportRepository airportRepository;
    @Autowired private FlightCopyRepository flightCopyRepository;
    @Autowired private RoutesAcrossReplicas routes;
    @Autowired private AirportListings airports;
    @Autowired private FlightCopiesAcrossReplicas flightCopies;
    @Autowired private ReadModelListener listener;
    @Autowired private ReadModelReconciler reconciler;
    @Autowired private PlatformTransactionManager transactions;

    private void inTransaction(Runnable action) {
        new TransactionTemplate(transactions).executeWithoutResult(status -> action.run());
    }

    @AfterEach
    void tearDown() {
        inTransaction(() -> {
            routeRepository.findAll().stream()
                    .filter(r -> r.getOrigin().getCode().equals("FAO"))
                    .forEach(r -> {
                        historyRepository.deleteAllByRoute("FAO", r.getDestination().getCode());
                        routeRepository.delete(r);
                    });
            flightCopyRepository.findById("rm-1").ifPresent(flightCopyRepository::delete);
            airportRepository.findByIataCode(new IataCode("ZZZ")).ifPresent(airportRepository::delete);
        });
    }

    /** The relay publishes through RabbitTemplate.invoke: what it sends is captured on these operations. */
    @BeforeEach
    @SuppressWarnings("unchecked")
    void captureWhatTheRelaySends() {
        when(rabbit.invoke(any(RabbitOperations.OperationsCallback.class)))
                .thenAnswer(i -> i.<RabbitOperations.OperationsCallback<?>>getArgument(0).doInRabbit(operations));
    }

    /** The relay sends from its own thread, once the change is committed: waits for it. */
    private Message lastEvent(String routingKey) {
        ArgumentCaptor<Message> message = ArgumentCaptor.forClass(Message.class);
        verify(operations, timeout(5_000).atLeastOnce()).send(eq("airports-routes.events"), eq(routingKey), message.capture());
        return message.getValue();
    }

    private List<String> routeKeys() {
        return routes.findAll().stream().map(Versioned::value)
                .map(r -> r.getOrigin().getCode() + "-" + r.getDestination().getCode()).sorted().toList();
    }

    @Test
    void ensureListingsReadTheReadModel() {
        assertInstanceOf(ReadModelRoutes.class, routes);
        assertInstanceOf(ReadModelAirportListings.class, airports);
        assertInstanceOf(ReadModelFlightCopies.class, flightCopies);
    }

    @Test
    void ensureReconciliationFillsTheReadModelWithWhatIsHeldHere() {
        // A route with history: exporting it reads the history, outside any request or transaction
        inTransaction(() -> {
            routeRepository.save(new Route("FAO", "OPO", 50, 300.0, 80));
            historyRepository.save(new RouteHistory(new IataCode("FAO"), new IataCode("OPO"), "created",
                    LocalDateTime.of(2026, 1, 1, 10, 0), "atcc"));
        });

        reconciler.reconcile();

        assertEquals(routeRepository.findAll().stream()
                .map(r -> r.getOrigin().getCode() + "-" + r.getDestination().getCode()).sorted().toList(), routeKeys());
        int airportsHeld = new TransactionTemplate(transactions).execute(status -> airportRepository.findAll().size());
        assertEquals(airportsHeld, airports.findAll().size());
    }

    @Test
    void ensureRouteEventsReachTheReadModel() {
        inTransaction(() -> routeRepository.save(new Route("FAO", "LIS", 40, 250.0, 80)));
        listener.onEvent(lastEvent("route.created"));
        assertTrue(routeKeys().contains("FAO-LIS"));

        Route changed = routeRepository.findByOriginAndDestination(new IataCode("FAO"), new IataCode("LIS")).orElseThrow();
        changed.changeStatus(RouteStatus.INACTIVE);
        inTransaction(() -> routeRepository.save(changed));
        listener.onEvent(lastEvent("route.updated"));
        assertEquals(RouteStatus.INACTIVE, routes.findAll().stream().map(Versioned::value)
                .filter(r -> r.getOrigin().getCode().equals("FAO")).findFirst().orElseThrow().getStatus());

        inTransaction(() -> routeRepository.delete(changed));
        listener.onEvent(lastEvent("route.deleted"));
        assertFalse(routeKeys().contains("FAO-LIS"));
    }

    @Test
    void ensureAirportEventsReachTheListings() {
        inTransaction(() -> airportRepository.save(new Airport("ZZZ", "Test Field", "Nowhere", "Testland", "Test Region",
                "UTC", 1.0, 2.0, List.of(new Runway("09/27", 2000, "090/270")))));
        listener.onEvent(lastEvent("airport.created"));

        List<AirportResponse> found = airports.search("test field", null, null, 0, 10).data();
        assertEquals(List.of("ZZZ"), found.stream().map(AirportResponse::iataCode).toList());
        assertEquals("Testland", found.get(0).country());

        inTransaction(() -> airportRepository.delete(airportRepository.findByIataCode(new IataCode("ZZZ")).orElseThrow()));
        listener.onEvent(lastEvent("airport.deleted"));
        assertTrue(airports.search("test field", null, null, 0, 10).data().isEmpty());
    }

    @Test
    void ensureFlightCopyEventsReachTheReadModel() {
        OffsetDateTime departure = OffsetDateTime.of(2027, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC);
        inTransaction(() -> flightCopyRepository.save(new FlightCopy("rm-1", departure, departure.plusHours(1),
                FlightCopyStatus.SCHEDULED, "OPO", "LIS", "CS-TKA", 1)));
        listener.onEvent(lastEvent("flightcopy.created"));

        FlightCopy copy = flightCopies.findAll().stream().filter(c -> c.getId().equals("rm-1")).findFirst().orElseThrow();
        assertEquals("OPO", copy.getOriginCode());
        assertEquals(FlightCopyStatus.SCHEDULED, copy.getStatus());
    }
}
