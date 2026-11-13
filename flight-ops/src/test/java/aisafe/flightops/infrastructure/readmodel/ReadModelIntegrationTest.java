package aisafe.flightops.infrastructure.readmodel;

import aisafe.fleet.application.Fleet;
import aisafe.fleet.infrastructure.messaging.MessagingFleet;
import aisafe.flights.application.FlightsAcrossReplicas;
import aisafe.flights.application.GenerateFlightUtilizationReportUseCase;
import aisafe.flights.application.ViewScheduledFlightsByAircraftUseCase;
import aisafe.flights.application.dtos.FlightResponse;
import aisafe.flights.application.dtos.FlightUtilizationResponse;
import aisafe.flights.domain.FlightStatus;
import aisafe.flights.domain.IataCode;
import aisafe.flights.domain.RegistrationNumber;
import aisafe.flights.domain.ScheduledFlight;
import aisafe.flights.domain.ScheduledFlightRepository;
import aisafe.flights.infrastructure.readmodel.FlightProjection;
import aisafe.flights.infrastructure.readmodel.ReadModelFlights;
import aisafe.shared.application.Versioned;
import aisafe.shared.domain.PaginatedResult;
import aisafe.shared.infrastructure.readmodel.ReadModelListener;
import aisafe.shared.infrastructure.readmodel.ReadModelReconciler;
import aisafe.shared.infrastructure.readmodel.ReadModelStore;
import aisafe.shared.infrastructure.readmodel.SnapshotHandler;
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
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Events on, without a broker: what the relay sends to RabbitMQ is captured and handed to the
 * listener by hand. This replica is flight-ops-0 in a ring of three (the peers do not exist).
 * Every write in its own transaction, as the use cases do: the events leave after the commit.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:fo-readmodel-it",
        "aisafe.events.enabled=true",
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "aisafe.node.id=flight-ops-0",
        "aisafe.peers=http://flight-ops-0.test.invalid:1,http://flight-ops-1.test.invalid:1,http://flight-ops-2.test.invalid:1",
        "aisafe.replication.dispatch-interval-ms=3600000",
        "aisafe.read-model.reconcile-interval-ms=3600000",
        "aisafe.bootstrap.seed-node=none"
})
@ActiveProfiles("jpa")
class ReadModelIntegrationTest {

    @MockitoBean private RabbitTemplate rabbit;
    private final RabbitOperations operations = mock(RabbitOperations.class);

    @Autowired private ScheduledFlightRepository flightRepository;
    @Autowired private FlightsAcrossReplicas flights;
    @Autowired private Fleet fleet;
    @Autowired private ReadModelListener listener;
    @Autowired private ReadModelReconciler reconciler;
    @Autowired private ReadModelStore store;
    @Autowired private SnapshotHandler snapshots;
    @Autowired private ViewScheduledFlightsByAircraftUseCase viewFlightsByAircraft;
    @Autowired private GenerateFlightUtilizationReportUseCase utilizationReport;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PlatformTransactionManager transactions;

    private void inTransaction(Runnable action) {
        new TransactionTemplate(transactions).executeWithoutResult(status -> action.run());
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    void captureWhatTheRelaySends() {
        when(rabbit.invoke(any(RabbitOperations.OperationsCallback.class)))
                .thenAnswer(i -> i.<RabbitOperations.OperationsCallback<?>>getArgument(0).doInRabbit(operations));
    }

    @AfterEach
    void tearDown() {
        inTransaction(() -> {
            flightRepository.findAll().forEach(flightRepository::delete);
        });
    }

    /** The relay sends from its own thread, once the change is committed: waits for it. */
    private Message lastEvent(String routingKey) {
        ArgumentCaptor<Message> message = ArgumentCaptor.forClass(Message.class);
        verify(operations, timeout(5_000).atLeastOnce())
                .send(eq("flight-ops.events"), eq(routingKey), message.capture());
        return message.getValue();
    }

    @Test
    void ensureListingsReadTheReadModel() {
        assertInstanceOf(ReadModelFlights.class, flights);
        assertInstanceOf(MessagingFleet.class, fleet);
    }

    @Test
    void ensureReconciliationFillsTheReadModelWithWhatIsHeldHere() {
        OffsetDateTime departure = OffsetDateTime.of(2027, 4, 1, 10, 0, 0, 0, ZoneOffset.UTC);
        ScheduledFlight flight = new ScheduledFlight(
                departure,
                departure.plusHours(2),
                FlightStatus.SCHEDULED,
                new IataCode("LIS"),
                new IataCode("OPO"),
                new RegistrationNumber("CS-TPA")
        );
        inTransaction(() -> flightRepository.save(flight));

        reconciler.reconcile();

        List<Versioned<ScheduledFlight>> all = flights.findAll();
        assertEquals(1, all.size());
        ScheduledFlight found = all.get(0).value();
        assertEquals("CS-TPA", found.getAircraftRegistrationNumber().getNumber());
        assertEquals("LIS", found.getOriginCode().getCode());
        assertEquals("OPO", found.getDestinationCode().getCode());
        assertEquals(FlightStatus.SCHEDULED, found.getStatus());
    }

    @Test
    void ensureFlightEventsReachTheReadModel() {
        OffsetDateTime departure = OffsetDateTime.of(2027, 5, 10, 8, 0, 0, 0, ZoneOffset.UTC);
        ScheduledFlight flight = new ScheduledFlight(
                departure,
                departure.plusHours(1),
                FlightStatus.SCHEDULED,
                new IataCode("OPO"),
                new IataCode("MAD"),
                new RegistrationNumber("CS-TPB")
        );

        // 1. Create -> flight.created event
        inTransaction(() -> flightRepository.save(flight));
        listener.onEvent(lastEvent("flight.created"));

        List<Versioned<ScheduledFlight>> afterCreate = flights.findAll();
        assertEquals(1, afterCreate.size());
        ScheduledFlight saved = afterCreate.get(0).value();
        assertEquals("CS-TPB", saved.getAircraftRegistrationNumber().getNumber());
        assertEquals(FlightStatus.SCHEDULED, saved.getStatus());

        // 2. Update -> flight.updated event
        saved.cancel();
        inTransaction(() -> flightRepository.save(saved));
        listener.onEvent(lastEvent("flight.updated"));

        List<Versioned<ScheduledFlight>> afterUpdate = flights.findAll();
        assertEquals(1, afterUpdate.size());
        assertEquals(FlightStatus.CANCELED, afterUpdate.get(0).value().getStatus());

        // 3. Delete -> flight.deleted event
        inTransaction(() -> flightRepository.delete(saved));
        listener.onEvent(lastEvent("flight.deleted"));

        List<Versioned<ScheduledFlight>> afterDelete = flights.findAll();
        assertTrue(afterDelete.isEmpty());
    }

    @Test
    void ensureSnapshotHandlerServesFlights() {
        OffsetDateTime departure = OffsetDateTime.of(2027, 7, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        ScheduledFlight flight = new ScheduledFlight(
                departure,
                departure.plusHours(3),
                FlightStatus.SCHEDULED,
                new IataCode("LIS"),
                new IataCode("CDG"),
                new RegistrationNumber("CS-TPC")
        );
        inTransaction(() -> flightRepository.save(flight));
        reconciler.reconcile();

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("entity", FlightProjection.ENTITY);

        List<ReadModelStore.StampedRow> snapshotRows = snapshots.handle(payload);
        assertFalse(snapshotRows.isEmpty());
        assertTrue(snapshotRows.stream().anyMatch(r -> r.view() != null && r.view().has("aircraftRegistration")
                && r.view().get("aircraftRegistration").asString().equals("CS-TPC")));
    }

    @Test
    void ensureUseCasesWorkWithReadModel() {
        // Populate read model with external aircraft (as if received via aircraft.* event)
        ObjectNode aircraftView = objectMapper.createObjectNode();
        aircraftView.put("registrationNumber", "CS-TPA");
        aircraftView.put("status", "AVAILABLE");
        aircraftView.put("modelName", "Airbus A320neo");
        aircraftView.put("seatCapacity", 180);
        aircraftView.put("range", 6000.0);
        store.apply("aircraft", "CS-TPA", aircraftView, 1L, System.currentTimeMillis(), "aircraft-maintenance-0");

        ObjectNode modelView = objectMapper.createObjectNode();
        modelView.put("modelName", "Airbus A320neo");
        modelView.put("fuelCapacity", 24000.0);
        store.apply("aircraftmodel", "Airbus A320neo", modelView, 1L, System.currentTimeMillis(), "aircraft-maintenance-0");

        // Save a completed flight locally and reconcile into read model
        OffsetDateTime departure = OffsetDateTime.of(2027, 8, 1, 9, 0, 0, 0, ZoneOffset.UTC);
        ScheduledFlight flight = new ScheduledFlight(
                departure,
                departure.plusHours(2),
                FlightStatus.COMPLETED,
                new IataCode("LIS"),
                new IataCode("OPO"),
                new RegistrationNumber("CS-TPA")
        );
        inTransaction(() -> flightRepository.save(flight));
        reconciler.reconcile();

        // Test ViewScheduledFlightsByAircraftUseCase (US213)
        List<FlightResponse> aircraftFlights = viewFlightsByAircraft.execute("CS-TPA");
        assertEquals(1, aircraftFlights.size());
        assertEquals("CS-TPA", aircraftFlights.get(0).aircraftId());
        assertEquals("LIS", aircraftFlights.get(0).originIataCode());
        assertEquals("OPO", aircraftFlights.get(0).destinationIataCode());

        // Test GenerateFlightUtilizationReportUseCase (US229)
        PaginatedResult<FlightUtilizationResponse> report = utilizationReport.execute(
                departure.minusDays(1), departure.plusDays(1), 0, 10);
        assertFalse(report.data().isEmpty());
        assertTrue(report.data().stream().anyMatch(r -> r.origin().equals("LIS")
                && r.destination().equals("OPO")));
    }
}
