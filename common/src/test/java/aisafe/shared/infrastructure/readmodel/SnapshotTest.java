package aisafe.shared.infrastructure.readmodel;

import aisafe.shared.infrastructure.messaging.CommandBus;
import aisafe.shared.infrastructure.messaging.CommandReply;
import aisafe.shared.infrastructure.messaging.CommandUnavailableException;
import aisafe.shared.infrastructure.replication.ReplicaHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** The owner's answer to a snapshot ({@link SnapshotHandler}) and the reader that applies it ({@link SnapshotClient}). */
class SnapshotTest {

    private final JsonMapper json = JsonMapper.builder().build();
    private final ReadModelStore store = mock(ReadModelStore.class);
    private final CommandBus commands = mock(CommandBus.class);

    private static final ExternalProjection AIRCRAFT = new ExternalProjection() {
        @Override public String entity() { return "aircraft"; }
        @Override public String source() { return "aircraft-maintenance"; }
    };

    private static Projection own(String entity) {
        return new Projection() {
            @Override public String entity() { return entity; }
            @Override public ReplicaHandler<?> handler() { return null; }
            @Override public Object view(JsonNode replica) { return replica; }
        };
    }

    @SuppressWarnings("unchecked")
    private SnapshotHandler handler(Projection... projections) {
        ObjectProvider<Projection> provider = mock(ObjectProvider.class);
        when(provider.orderedStream()).thenAnswer(i -> Stream.of(projections));
        return new SnapshotHandler(store, provider);
    }

    @Test
    void ensureOwnerAnswersWithEveryRowOfTheEntityDeletedOnesIncluded() {
        List<ReadModelStore.StampedRow> rows = List.of(
                new ReadModelStore.StampedRow("CS-TKA", json.createObjectNode().put("status", "AVAILABLE"), 2L, 10, "am-0"),
                new ReadModelStore.StampedRow("CS-OLD", null, null, 20, "am-1"));
        when(store.findAllStamped("aircraft")).thenReturn(rows);

        assertEquals(rows, handler(own("aircraft")).handle(json.createObjectNode().put("entity", "aircraft")));
    }

    @Test
    void ensureOwnerRefusesAnEntityItDoesNotHave() {
        assertThrows(IllegalArgumentException.class,
                () -> handler(own("aircraft")).handle(json.createObjectNode().put("entity", "route")));
        assertThrows(IllegalArgumentException.class, () -> handler(own("aircraft")).handle(json.createObjectNode()));
    }

    private SnapshotClient client(Clock clock) {
        return new SnapshotClient(List.of(AIRCRAFT), commands, store, json, 300_000, clock);
    }

    private CommandReply answer(Object rows) {
        return new CommandReply(200, json.valueToTree(rows), null);
    }

    @Test
    void ensureEveryRowOfTheAnswerIsAppliedWithItsStamp() {
        when(commands.send("aircraft-maintenance", "snapshot", Map.of("entity", "aircraft"))).thenReturn(answer(List.of(
                new ReadModelStore.StampedRow("CS-TKA", json.createObjectNode().put("status", "AVAILABLE"), 2L, 10, "am-0"),
                new ReadModelStore.StampedRow("CS-OLD", null, null, 20, "am-1"))));

        client(Clock.systemUTC()).fill(AIRCRAFT);

        verify(store).apply(eq("aircraft"), eq("CS-TKA"), eq(json.createObjectNode().put("status", "AVAILABLE")), eq(2L), eq(10L), eq("am-0"));
        // A deleted row: its view comes as a JSON null, which the store takes as a deletion
        verify(store).apply(eq("aircraft"), eq("CS-OLD"), argThat(view -> view == null || view.isNull()), eq(null), eq(20L), eq("am-1"));
    }

    @Test
    void ensureOwnerNotAnsweringLeavesTheCopyAsItIsAndAsksAgainOnTheNextRound() {
        when(commands.send(anyString(), anyString(), any())).thenThrow(new CommandUnavailableException("no answer"));
        SnapshotClient client = client(Clock.systemUTC());

        client.fill(AIRCRAFT);
        client.refresh();

        verify(commands, times(2)).send(anyString(), anyString(), any());
        verifyNoInteractions(store);
    }

    @Test
    void ensureEmptyAnswerIsAskedAgainOnTheNextRound() {
        when(commands.send(anyString(), anyString(), any())).thenReturn(answer(List.of()));
        SnapshotClient client = client(Clock.fixed(Instant.parse("2026-11-04T10:00:00Z"), ZoneOffset.UTC));

        client.fill(AIRCRAFT);
        client.refresh();

        verify(commands, times(2)).send(anyString(), anyString(), any());
    }

    @Test
    void ensureAfterAnAnswerItWaitsForTheIntervalBeforeAskingAgain() {
        when(commands.send(anyString(), anyString(), any())).thenReturn(answer(List.of(
                new ReadModelStore.StampedRow("CS-TKA", json.createObjectNode(), 1L, 10, "am-0"))));
        Instant start = Instant.parse("2026-11-04T10:00:00Z");
        SnapshotClient soon = client(Clock.fixed(start, ZoneOffset.UTC));
        soon.fill(AIRCRAFT);

        soon.refresh();
        verify(commands, times(1)).send(anyString(), anyString(), any());
    }

    @Test
    void ensureRefusedSnapshotAppliesNothing() {
        when(commands.send(anyString(), anyString(), any())).thenReturn(new CommandReply(400, null, "No snapshot"));

        client(Clock.systemUTC()).fill(AIRCRAFT);

        verify(store, never()).apply(anyString(), anyString(), any(), any(), anyLong(), anyString());
    }
}
