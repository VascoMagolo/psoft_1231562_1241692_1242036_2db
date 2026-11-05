package aisafe.airportsroutes.infrastructure.readmodel;

import aisafe.shared.infrastructure.readmodel.ReadModelStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("jpa")
@Import({ReadModelStore.class, ReadModelStoreTest.Json.class})
class ReadModelStoreTest {

    static class Json {
        @org.springframework.context.annotation.Bean
        JsonMapper jsonMapper() {
            return JsonMapper.builder().build();
        }
    }

    @Autowired
    private ReadModelStore store;

    private static JsonNode view(int flightTime) {
        return JsonMapper.builder().build().createObjectNode().put("estimatedFlightTime", flightTime);
    }

    private int flightTimeOf(String type, String key) {
        return store.findAll(type).stream().filter(r -> r.key().equals(key)).findFirst().orElseThrow()
                .view().get("estimatedFlightTime").asInt();
    }

    @Test
    void ensureAppliedEntityIsListedWithItsVersion() {
        assertTrue(store.apply("route", "OPO-LIS", view(55), 2L, 1_000, "airports-routes-0"));

        List<ReadModelStore.Row> rows = store.findAll("route");
        assertEquals(1, rows.size());
        assertEquals("OPO-LIS", rows.get(0).key());
        assertEquals(55, rows.get(0).view().get("estimatedFlightTime").asInt());
        assertEquals(2L, rows.get(0).version());
    }

    @Test
    void ensureTypesAreKeptApart() {
        store.apply("route", "LIS", view(1), 0L, 1_000, "a");
        store.apply("airport", "LIS", view(2), null, 1_000, "a");

        assertEquals(1, flightTimeOf("route", "LIS"));
        assertEquals(2, flightTimeOf("airport", "LIS"));
    }

    @Test
    void ensureLaterChangeReplacesTheEntity() {
        store.apply("route", "OPO-LIS", view(55), 0L, 1_000, "airports-routes-0");

        assertTrue(store.apply("route", "OPO-LIS", view(60), 1L, 2_000, "airports-routes-1"));

        assertEquals(60, flightTimeOf("route", "OPO-LIS"));
    }

    @Test
    void ensureOlderChangeArrivingLateIsIgnored() {
        store.apply("route", "OPO-LIS", view(60), 1L, 2_000, "airports-routes-0");

        assertFalse(store.apply("route", "OPO-LIS", view(55), 0L, 1_000, "airports-routes-0"));

        assertEquals(60, flightTimeOf("route", "OPO-LIS"));
    }

    @Test
    void ensureSameTimeIsDecidedByTheReplicaId() {
        store.apply("route", "OPO-LIS", view(55), 0L, 1_000, "airports-routes-0");

        assertTrue(store.apply("route", "OPO-LIS", view(60), 0L, 1_000, "airports-routes-1"));
        assertFalse(store.apply("route", "OPO-LIS", view(70), 0L, 1_000, "airports-routes-0"));

        assertEquals(60, flightTimeOf("route", "OPO-LIS"));
    }

    @Test
    void ensureDeletedEntityIsNotListedAndOlderSaveDoesNotBringItBack() {
        store.apply("route", "OPO-LIS", view(55), 0L, 1_000, "airports-routes-0");

        assertTrue(store.apply("route", "OPO-LIS", null, null, 2_000, "airports-routes-0"));
        assertTrue(store.findAll("route").isEmpty());

        assertFalse(store.apply("route", "OPO-LIS", view(55), 0L, 1_500, "airports-routes-1"));
        assertTrue(store.findAll("route").isEmpty());
    }

    @Test
    void ensureEntityCreatedAgainAfterDeleteIsListed() {
        store.apply("route", "OPO-LIS", null, null, 2_000, "airports-routes-0");

        assertTrue(store.apply("route", "OPO-LIS", view(55), 0L, 3_000, "airports-routes-0"));

        assertEquals(1, store.findAll("route").size());
    }

    /** An event and the reconciliation applying the same key at once (e.g. right after a restart). */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void ensureTwoWritersOfTheSameKeyAlwaysLeaveTheLaterChange() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            for (int i = 0; i < 200; i++) {
                String key = "RACE-" + i;
                CyclicBarrier together = new CyclicBarrier(2);
                Future<?> older = pool.submit(() -> { together.await(); return store.apply("race", key, view(1), 0L, 1_000, "a"); });
                Future<?> later = pool.submit(() -> { together.await(); return store.apply("race", key, view(2), 1L, 2_000, "a"); });
                older.get();
                later.get();
                assertEquals(2, flightTimeOf("race", key), "the later change stays (" + key + ")");
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void ensureTheSameChangeAppliedTwiceIsAppliedOnce() {
        assertTrue(store.apply("route", "OPO-LIS", view(55), 0L, 1_000, "a"));

        assertFalse(store.apply("route", "OPO-LIS", view(55), 0L, 1_000, "a"));
    }

    @Test
    void ensureEntitiesAreOrderedByKey() {
        store.apply("route", "OPO-MAD", view(1), 0L, 1, "a");
        store.apply("route", "LIS-OPO", view(1), 0L, 1, "a");
        store.apply("route", "OPO-LIS", view(1), 0L, 1, "a");

        assertEquals(List.of("LIS-OPO", "OPO-LIS", "OPO-MAD"),
                store.findAll("route").stream().map(ReadModelStore.Row::key).toList());
    }

    @Test
    void ensureJsonNullViewIsTakenAsADeletion() {
        store.apply("route", "OPO-FAO", view(10), 0L, 1_000, "a");

        assertTrue(store.apply("route", "OPO-FAO", JsonMapper.builder().build().nullNode(), null, 2_000, "a"));

        assertTrue(store.findAll("route").stream().noneMatch(r -> r.key().equals("OPO-FAO")));
        assertTrue(store.findAllStamped("route").stream().anyMatch(r -> r.key().equals("OPO-FAO") && r.view() == null));
    }
}
