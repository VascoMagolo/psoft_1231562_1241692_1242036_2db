package aisafe.routes.domain;

import aisafe.airports.domain.IataCode;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RouteHistoryTest {

    @Test
    void ensureRouteHistoryIsCreatedCorrectly() {
        IataCode originCode = new IataCode("LIS");
        IataCode destinationCode = new IataCode("OPO");
        String changeDescription = "Route created";
        String changedBy = "testUser";

        RouteHistory routeHistory = new RouteHistory(originCode, destinationCode, changeDescription, changedBy);

        assertNotNull(routeHistory);
        assertEquals(originCode, routeHistory.getOriginCode());
        assertEquals(destinationCode, routeHistory.getDestinationCode());
        assertEquals(changeDescription, routeHistory.getChangeDescription());
        assertEquals(changedBy, routeHistory.getChangedBy());
        assertNotNull(routeHistory.getChangedAt());
        assertTrue(routeHistory.getChangedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void ensureReconstitutionConstructorSetsChangedAt() {
        IataCode origin = new IataCode("LIS");
        IataCode destination = new IataCode("OPO");
        LocalDateTime specificTime = LocalDateTime.of(2023, 1, 1, 10, 0);

        RouteHistory routeHistory = new RouteHistory(origin, destination, "Desc", specificTime, "User");

        assertEquals(specificTime, routeHistory.getChangedAt());
    }

    @Test
    void ensureDefaultConstructorForJPA() {
        RouteHistory history = new RouteHistory();
        assertNotNull(history);
        assertNull(history.getOriginCode());
        assertNull(history.getDestinationCode());
        assertNull(history.getChangeDescription());
        assertNull(history.getChangedAt());
        assertNull(history.getChangedBy());
    }
}
