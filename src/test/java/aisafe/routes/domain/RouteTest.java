package aisafe.routes.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RouteTest {

    @Test
    void ensureValidRouteIsCreatedAndIsActive() {
        Route route = new Route("OPO", "LIS", 45, 300.0, 150);
        assertEquals("OPO", route.getOrigin().getCode());
        assertEquals("LIS", route.getDestination().getCode());
        assertEquals(RouteStatus.ACTIVE, route.getStatus());
    }

    @Test
    void ensureOriginAndDestinationCannotBeTheSame() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new Route("LIS", "LIS", 45, 300.0, 150));
        assertEquals("Origin and destination cannot be the same", ex.getMessage());
    }

    @Test
    void ensureZeroFlightTimeThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Route("OPO", "LIS", 0, 300.0, 150));
    }

    @Test
    void ensureNegativeFlightTimeThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Route("OPO", "LIS", -10, 300.0, 150));
    }

    @Test
    void ensureZeroRangeThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Route("OPO", "LIS", 45, 0.0, 150));
    }

    @Test
    void ensureZeroCapacityThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Route("OPO", "LIS", 45, 300.0, 0));
    }

    @Test
    void ensureSetStatusInactiveSetsRouteInactive() {
        Route route = new Route("OPO", "LIS", 45, 300.0, 150);
        route.setStatus(RouteStatus.INACTIVE);
        assertEquals(RouteStatus.INACTIVE, route.getStatus());
    }

    @Test
    void ensureUpdateRouteChangesFields() {
        Route route = new Route("OPO", "LIS", 45, 300.0, 150);
        route.updateRoute(60, 400.0, 180);
        assertEquals(60, route.getEstimatedFlightTime());
        assertEquals(400.0, route.getMinimumRange());
        assertEquals(180, route.getMinimumCapacity());
    }

    @Test
    void ensureUpdateRouteIgnoresNullFields() {
        Route route = new Route("OPO", "LIS", 45, 300.0, 150);
        route.updateRoute(null, null, null);
        assertEquals(45, route.getEstimatedFlightTime());
        assertEquals(300.0, route.getMinimumRange());
        assertEquals(150, route.getMinimumCapacity());
    }

    @Test
    void ensureUpdateRouteWithInvalidFlightTimeThrowsException() {
        Route route = new Route("OPO", "LIS", 45, 300.0, 150);
        assertThrows(IllegalArgumentException.class, () -> route.updateRoute(-5, null, null));
    }
}
