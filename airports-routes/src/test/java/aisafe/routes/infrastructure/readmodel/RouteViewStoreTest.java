package aisafe.routes.infrastructure.readmodel;

import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteStatus;
import aisafe.shared.application.Versioned;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("jpa")
@Import(RouteViewStore.class)
class RouteViewStoreTest {

    @Autowired
    private RouteViewStore store;

    private static RouteView opoLis(int flightTime, RouteStatus status) {
        return new RouteView("OPO", "LIS", flightTime, 300.0, 100, status);
    }

    @Test
    void ensureAppliedRouteIsListedWithItsVersion() {
        assertTrue(store.apply("OPO-LIS", opoLis(55, RouteStatus.ACTIVE), 2L, 1_000, "airports-routes-0"));

        List<Versioned<Route>> routes = store.findAll();
        assertEquals(1, routes.size());
        assertEquals("OPO", routes.get(0).value().getOrigin().getCode());
        assertEquals(55, routes.get(0).value().getEstimatedFlightTime());
        assertEquals(2L, routes.get(0).version());
    }

    @Test
    void ensureLaterChangeReplacesTheRoute() {
        store.apply("OPO-LIS", opoLis(55, RouteStatus.ACTIVE), 0L, 1_000, "airports-routes-0");

        assertTrue(store.apply("OPO-LIS", opoLis(60, RouteStatus.INACTIVE), 1L, 2_000, "airports-routes-1"));

        Route route = store.findAll().get(0).value();
        assertEquals(60, route.getEstimatedFlightTime());
        assertEquals(RouteStatus.INACTIVE, route.getStatus());
    }

    @Test
    void ensureOlderChangeArrivingLateIsIgnored() {
        store.apply("OPO-LIS", opoLis(60, RouteStatus.ACTIVE), 1L, 2_000, "airports-routes-0");

        assertFalse(store.apply("OPO-LIS", opoLis(55, RouteStatus.ACTIVE), 0L, 1_000, "airports-routes-0"));

        assertEquals(60, store.findAll().get(0).value().getEstimatedFlightTime());
    }

    @Test
    void ensureSameTimeIsDecidedByTheReplicaId() {
        store.apply("OPO-LIS", opoLis(55, RouteStatus.ACTIVE), 0L, 1_000, "airports-routes-0");

        assertTrue(store.apply("OPO-LIS", opoLis(60, RouteStatus.ACTIVE), 0L, 1_000, "airports-routes-1"));
        assertFalse(store.apply("OPO-LIS", opoLis(70, RouteStatus.ACTIVE), 0L, 1_000, "airports-routes-0"));

        assertEquals(60, store.findAll().get(0).value().getEstimatedFlightTime());
    }

    @Test
    void ensureDeletedRouteIsNotListedAndOlderSaveDoesNotBringItBack() {
        store.apply("OPO-LIS", opoLis(55, RouteStatus.ACTIVE), 0L, 1_000, "airports-routes-0");

        assertTrue(store.apply("OPO-LIS", null, null, 2_000, "airports-routes-0"));
        assertTrue(store.findAll().isEmpty());

        assertFalse(store.apply("OPO-LIS", opoLis(55, RouteStatus.ACTIVE), 0L, 1_500, "airports-routes-1"));
        assertTrue(store.findAll().isEmpty());
    }

    @Test
    void ensureRouteCreatedAgainAfterDeleteIsListed() {
        store.apply("OPO-LIS", null, null, 2_000, "airports-routes-0");

        assertTrue(store.apply("OPO-LIS", opoLis(55, RouteStatus.ACTIVE), 0L, 3_000, "airports-routes-0"));

        assertEquals(1, store.findAll().size());
    }

    @Test
    void ensureRoutesAreOrderedByOriginAndDestination() {
        store.apply("OPO-MAD", new RouteView("OPO", "MAD", 70, 400.0, 100, RouteStatus.ACTIVE), 0L, 1, "a");
        store.apply("LIS-OPO", new RouteView("LIS", "OPO", 55, 300.0, 100, RouteStatus.ACTIVE), 0L, 1, "a");
        store.apply("OPO-LIS", opoLis(55, RouteStatus.ACTIVE), 0L, 1, "a");

        assertEquals(List.of("LIS-OPO", "OPO-LIS", "OPO-MAD"), store.findAll().stream()
                .map(r -> r.value().getOrigin().getCode() + "-" + r.value().getDestination().getCode())
                .toList());
    }
}
