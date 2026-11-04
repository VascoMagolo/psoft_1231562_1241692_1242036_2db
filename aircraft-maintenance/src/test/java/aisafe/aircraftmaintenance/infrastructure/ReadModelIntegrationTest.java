package aisafe.aircraftmaintenance.infrastructure;

import aisafe.aircrafts.application.AircraftAcrossReplicas;
import aisafe.aircrafts.application.ViewAircraftMoodsUseCase;
import aisafe.aircrafts.application.dtos.AircraftMoodResponse;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelImage;
import aisafe.aircrafts.domain.AircraftModelRepository;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.AircraftStatus;
import aisafe.aircrafts.domain.Manufacturer;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.aircrafts.infrastructure.readmodel.ReadModelAircraft;
import aisafe.flightcopies.application.FlightCopiesAcrossReplicas;
import aisafe.flightcopies.domain.FlightCopy;
import aisafe.flightcopies.domain.FlightCopyRepository;
import aisafe.flightcopies.domain.FlightCopyStatus;
import aisafe.flightcopies.infrastructure.FlightCopyCommandHandler;
import aisafe.flightcopies.infrastructure.readmodel.ReadModelFlightCopies;
import aisafe.maintenance.application.MaintenanceRecordsAcrossReplicas;
import aisafe.maintenance.infrastructure.readmodel.ReadModelMaintenanceRecords;
import aisafe.shared.infrastructure.messaging.CommandDispatcher;
import aisafe.shared.infrastructure.messaging.CommandReply;
import aisafe.shared.infrastructure.messaging.Messaging;
import aisafe.shared.infrastructure.readmodel.ReadModelListener;
import aisafe.shared.infrastructure.readmodel.ReadModelReconciler;
import aisafe.shared.infrastructure.readmodel.ReadModelStore;
import aisafe.shared.infrastructure.readmodel.SnapshotHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
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

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Events on, without a broker: what the relay sends to RabbitMQ is captured and handed to the
 * listener by hand. This replica is aircraft-maintenance-0 in a ring of three (the peers do not
 * exist). Every write in its own transaction, as the use cases do: the events leave after the commit.
 */
@SpringBootTest(properties = {
        // A database of its own: its deletions leave tombstones that would hide the same keys from other tests
        "spring.datasource.url=jdbc:h2:mem:am-readmodel-it",
        "aisafe.events.enabled=true",
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "aisafe.node.id=aircraft-maintenance-0",
        "aisafe.peers=http://aircraft-maintenance-0.test.invalid:1,http://aircraft-maintenance-1.test.invalid:1,http://aircraft-maintenance-2.test.invalid:1",
        "aisafe.replication.dispatch-interval-ms=3600000",
        "aisafe.read-model.reconcile-interval-ms=3600000"
})
@ActiveProfiles("jpa")
class ReadModelIntegrationTest {

    @MockitoBean private RabbitTemplate rabbit;
    private final RabbitOperations operations = mock(RabbitOperations.class);

    @Autowired private AircraftRepository aircraftRepository;
    @Autowired private AircraftModelRepository modelRepository;
    @Autowired private FlightCopyRepository flightCopyRepository;
    @Autowired private AircraftAcrossReplicas aircraft;
    @Autowired private MaintenanceRecordsAcrossReplicas records;
    @Autowired private FlightCopiesAcrossReplicas flightCopies;
    @Autowired private ViewAircraftMoodsUseCase moods;
    @Autowired private ReadModelListener listener;
    @Autowired private ReadModelReconciler reconciler;
    @Autowired private ReadModelStore store;
    @Autowired private SnapshotHandler snapshots;
    @Autowired private CommandDispatcher commands;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PlatformTransactionManager transactions;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void captureWhatTheRelaySends() {
        when(rabbit.invoke(any(RabbitOperations.OperationsCallback.class)))
                .thenAnswer(i -> i.<RabbitOperations.OperationsCallback<?>>getArgument(0).doInRabbit(operations));
    }

    @AfterEach
    void tearDown() {
        inTransaction(() -> {
            for (String registration : List.of("CS-RMA")) {
                aircraftRepository.findByRegistrationNumber(new RegistrationNumber(registration)).ifPresent(aircraftRepository::delete);
            }
            modelRepository.findByModelName("RM Model").ifPresent(modelRepository::delete);
            flightCopyRepository.findById("rm-1").ifPresent(flightCopyRepository::delete);
            flightCopyRepository.findById("rm-cmd").ifPresent(flightCopyRepository::delete);
        });
    }

    private void inTransaction(Runnable action) {
        new TransactionTemplate(transactions).executeWithoutResult(status -> action.run());
    }

    /** The relay sends from its own thread, once the change is committed: waits for it. */
    private Message lastEvent(String routingKey) {
        ArgumentCaptor<Message> message = ArgumentCaptor.forClass(Message.class);
        verify(operations, timeout(5_000).atLeastOnce())
                .send(eq("aircraft-maintenance.events"), eq(routingKey), message.capture());
        return message.getValue();
    }

    private AircraftModel a320() {
        return modelRepository.findByModelName("Airbus A320neo").orElseThrow();
    }

    private AircraftMoodResponse moodOf(String registration) {
        return moods.execute().stream().filter(m -> m.registrationNumber().equals(registration)).findFirst().orElseThrow();
    }

    @Test
    void ensureQueriesReadTheReadModel() {
        assertInstanceOf(ReadModelAircraft.class, aircraft);
        assertInstanceOf(ReadModelMaintenanceRecords.class, records);
        assertInstanceOf(ReadModelFlightCopies.class, flightCopies);
    }

    @Test
    void ensureAircraftEventsReachTheFleetAndTheUnicorns() {
        inTransaction(() -> aircraftRepository.save(new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2022, 1, 1), a320(),
                new RegistrationNumber("CS-RMA"), 150, 5000.0, List.of())));
        listener.onEvent(lastEvent("aircraft.created"));

        assertTrue(aircraft.findAll().stream().anyMatch(a -> a.getRegistrationNumber().getNumber().equals("CS-RMA")));
        assertEquals("HAPPY", moodOf("CS-RMA").mood());

        inTransaction(() -> {
            Aircraft stored = aircraftRepository.findByRegistrationNumber(new RegistrationNumber("CS-RMA")).orElseThrow();
            aircraftRepository.save(new Aircraft(AircraftStatus.UNDER_MAINTENANCE, stored.getManufacturingDate(),
                    a320(), stored.getRegistrationNumber(), stored.getSeatCapacity(), stored.getRange(), stored.getFeatures()));
        });
        listener.onEvent(lastEvent("aircraft.updated"));
        assertEquals("GRUMPY", moodOf("CS-RMA").mood());

        inTransaction(() -> aircraftRepository.delete(aircraftRepository.findByRegistrationNumber(new RegistrationNumber("CS-RMA")).orElseThrow()));
        listener.onEvent(lastEvent("aircraft.deleted"));
        assertTrue(aircraft.findAll().stream().noneMatch(a -> a.getRegistrationNumber().getNumber().equals("CS-RMA")));
    }

    @Test
    void ensureAircraftEventCarriesWhatTheOtherServicesNeed() {
        inTransaction(() -> aircraftRepository.save(new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2022, 1, 1), a320(),
                new RegistrationNumber("CS-RMA"), 150, 5000.0, List.of("WiFi"))));

        JsonNode event = objectMapper.readTree(new String(lastEvent("aircraft.created").getBody(), StandardCharsets.UTF_8));
        assertEquals("CS-RMA", event.get("key").asString());
        assertEquals("AVAILABLE", event.get("status").asString());
        JsonNode data = event.get("data");
        assertEquals("Airbus A320neo", data.get("modelName").asString());
        assertEquals(150, data.get("seatCapacity").asInt());
        assertEquals(5000.0, data.get("range").asDouble());
    }

    @Test
    void ensureModelEventsCarryNoImageBytes() {
        inTransaction(() -> modelRepository.save(new AircraftModel("RM Model", Manufacturer.AIRBUS, 1000.0, 3000.0, 700.0,
                new AircraftModelImage(new byte[]{1, 2, 3}, "image/png"), 100)));

        JsonNode data = objectMapper.readTree(new String(lastEvent("aircraftmodel.created").getBody(), StandardCharsets.UTF_8))
                .get("data");
        assertEquals("RM Model", data.get("modelName").asString());
        assertEquals(1000.0, data.get("fuelCapacity").asDouble());
        assertTrue(data.get("hasImage").asBoolean());
        assertNull(data.get("imageBytes"));
    }

    @Test
    void ensureFlightCopyEventsReachTheReadModel() {
        OffsetDateTime departure = OffsetDateTime.of(2027, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC);
        inTransaction(() -> flightCopyRepository.save(new FlightCopy("rm-1", departure, departure.plusHours(1),
                FlightCopyStatus.SCHEDULED, "OPO", "LIS", new RegistrationNumber("CS-TKA"), 1)));
        listener.onEvent(lastEvent("flightcopy.created"));

        assertTrue(flightCopies.findAll().stream().anyMatch(c -> c.getId().equals("rm-1")));
    }

    @Test
    void ensureReconciliationFillsTheReadModelAndSnapshotsAnswerFromIt() {
        reconciler.reconcile();

        int held = new TransactionTemplate(transactions).execute(status -> aircraftRepository.findAll().size());
        assertEquals(held, aircraft.findAll().size());
        List<ReadModelStore.StampedRow> rows = snapshots.handle(objectMapper.createObjectNode().put("entity", "aircraft"));
        assertTrue(rows.stream().anyMatch(r -> r.key().equals("CS-TKA") && r.view() != null && r.stamp() > 0));
        assertTrue(snapshots.handle(objectMapper.createObjectNode().put("entity", "aircraftmodel")).stream()
                .anyMatch(r -> r.key().equals("Airbus A320neo")));
    }

    /** As flight-ops sends it: the command dispatcher takes it from the queue and answers. */
    private CommandReply c4(Map<String, Object> copy) {
        MessageProperties properties = new MessageProperties();
        properties.setHeader(Messaging.COMMAND_TYPE_HEADER, FlightCopyCommandHandler.TYPE);
        properties.setHeader(Messaging.SENDER_HEADER, "flight-ops");
        Message answer = commands.onCommand(new Message(objectMapper.writeValueAsBytes(copy), properties));
        return objectMapper.readValue(answer.getBody(), CommandReply.class);
    }

    @Test
    void ensureC4CommandStoresTheCopyOrAnswersAsTheHttpRoute() {
        Map<String, Object> copy = Map.of("id", "rm-cmd", "departureDateTime", "2027-02-01T10:00:00Z",
                "arrivalDateTime", "2027-02-01T12:00:00Z", "status", "SCHEDULED", "originIataCode", "OPO",
                "destinationIataCode", "LIS", "aircraftRegistration", "CS-TKA", "version", 1);

        CommandReply stored = c4(copy);
        assertEquals(200, stored.status(), stored.error());
        assertEquals("rm-cmd", stored.body().get("id").asString());
        assertTrue(flightCopyRepository.findById("rm-cmd").isPresent());

        Map<String, Object> unknownAircraft = new java.util.HashMap<>(copy);
        unknownAircraft.put("id", "rm-cmd-2");
        unknownAircraft.put("aircraftRegistration", "CS-ZZZ");
        assertEquals(404, c4(unknownAircraft).status());

        Map<String, Object> invalid = new java.util.HashMap<>(copy);
        invalid.remove("departureDateTime");
        assertEquals(400, c4(invalid).status());
    }

    /** C1 and C3 as queries over the broker, for a copy that does not have the key yet. */
    @Test
    void ensureFleetQueriesAnswerFromTheWriteModel() {
        MessageProperties properties = new MessageProperties();
        properties.setHeader(Messaging.COMMAND_TYPE_HEADER, "aircraft.get");
        CommandReply aircraftReply = objectMapper.readValue(commands.onCommand(new Message(
                objectMapper.writeValueAsBytes(Map.of("key", "cs-tka")), properties)).getBody(), CommandReply.class);
        assertEquals(200, aircraftReply.status());
        assertEquals("CS-TKA", aircraftReply.body().get("registrationNumber").asString());
        assertEquals("Airbus A320neo", aircraftReply.body().get("model").asString());

        CommandReply unknown = objectMapper.readValue(commands.onCommand(new Message(
                objectMapper.writeValueAsBytes(Map.of("key", "CS-ZZZ")), properties)).getBody(), CommandReply.class);
        assertEquals(404, unknown.status());

        MessageProperties model = new MessageProperties();
        model.setHeader(Messaging.COMMAND_TYPE_HEADER, "aircraftmodel.get");
        CommandReply modelReply = objectMapper.readValue(commands.onCommand(new Message(
                objectMapper.writeValueAsBytes(Map.of("key", "Airbus A320neo")), model)).getBody(), CommandReply.class);
        assertEquals(200, modelReply.status());
    }
}
