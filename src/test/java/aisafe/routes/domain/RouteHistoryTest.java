package aisafe.routes.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RouteHistoryTest {

    @Test
    void ensureRouteHistoryIsCreatedCorrectly() {
        String originCode = "LIS";
        String destinationCode = "OPO";
        String changeDescription = "Route created";
        String changedBy = "testUser";

        RouteHistory routeHistory = new RouteHistory(originCode, destinationCode, changeDescription, changedBy);

        assertNotNull(routeHistory);
        assertEquals(originCode, routeHistory.getOriginCode());
        assertEquals(destinationCode, routeHistory.getDestinationCode());
        assertEquals(changeDescription, routeHistory.getChangeDescription());
        assertEquals(changedBy, routeHistory.getChangedBy());
        assertNotNull(routeHistory.getChangedAt());
        assertTrue(routeHistory.getChangedAt().isBefore(LocalDateTime.now().plusSeconds(1))); // ensure changedAt is set around now
    }

    @Test
    void ensureSetChangedAtUpdatesValue() {
        RouteHistory routeHistory = new RouteHistory("LIS", "OPO", "Desc", "User");
        LocalDateTime specificTime = LocalDateTime.of(2023, 1, 1, 10, 0);
        routeHistory.setChangedAt(specificTime);
        assertEquals(specificTime, routeHistory.getChangedAt());
    }
}
