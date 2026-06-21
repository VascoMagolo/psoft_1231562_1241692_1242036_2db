package aisafe.routes.domain;

import aisafe.shared.domain.DomainException;
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
        DomainException ex = assertThrows(DomainException.class, () ->
                new Route("LIS", "LIS", 45, 300.0, 150));
        assertEquals("Origin and destination cannot be the same", ex.getMessage());
    }

    @Test
    void ensureZeroFlightTimeThrowsException() {
        assertThrows(DomainException.class, () ->
                new Route("OPO", "LIS", 0, 300.0, 150));
    }

    @Test
    void ensureNegativeFlightTimeThrowsException() {
        assertThrows(DomainException.class, () ->
                new Route("OPO", "LIS", -10, 300.0, 150));
    }

    @Test
    void ensureZeroRangeThrowsException() {
        assertThrows(DomainException.class, () ->
                new Route("OPO", "LIS", 45, 0.0, 150));
    }

    @Test
    void ensureZeroCapacityThrowsException() {
        assertThrows(DomainException.class, () ->
                new Route("OPO", "LIS", 45, 300.0, 0));
    }

    @Test
    void ensureSetStatusInactiveSetsRouteInactive() {
        Route route = new Route("OPO", "LIS", 45, 300.0, 150);
        route.changeStatus(RouteStatus.INACTIVE);
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
        assertThrows(DomainException.class, () -> route.updateRoute(-5, null, null));
    }

    @Test
    void ensureDefaultConstructorForJPA() {
        Route route = new Route();
        assertNotNull(route);
        assertNull(route.getOrigin());
        assertNull(route.getDestination());
    }

    @Test
    void ensureConstructorThrowsWhenOriginIsNull() {
        assertThrows(DomainException.class, () -> new Route(null, "LIS", 45, 300.0, 150));
    }

    @Test
    void ensureConstructorThrowsWhenOriginIsBlank() {
        assertThrows(DomainException.class, () -> new Route("   ", "LIS", 45, 300.0, 150));
    }

    @Test
    void ensureConstructorThrowsWhenDestinationIsNull() {
        assertThrows(DomainException.class, () -> new Route("OPO", null, 45, 300.0, 150));
    }

    @Test
    void ensureConstructorThrowsWhenDestinationIsBlank() {
        assertThrows(DomainException.class, () -> new Route("OPO", "   ", 45, 300.0, 150));
    }

    @Test
    void ensureConstructorThrowsWhenFlightTimeIsNull() {
        assertThrows(DomainException.class, () -> new Route("OPO", "LIS", null, 300.0, 150));
    }

    @Test
    void ensureConstructorThrowsWhenRangeIsNull() {
        assertThrows(DomainException.class, () -> new Route("OPO", "LIS", 45, null, 150));
    }

    @Test
    void ensureConstructorThrowsWhenCapacityIsNull() {
        assertThrows(DomainException.class, () -> new Route("OPO", "LIS", 45, 300.0, null));
    }

    @Test
    void ensureUpdateRouteWithInvalidRangeThrowsException() {
        Route route = new Route("OPO", "LIS", 45, 300.0, 150);
        assertThrows(DomainException.class, () -> route.updateRoute(null, -10.0, null));
        assertThrows(DomainException.class, () -> route.updateRoute(null, 0.0, null));
    }

    @Test
    void ensureUpdateRouteWithInvalidCapacityThrowsException() {
        Route route = new Route("OPO", "LIS", 45, 300.0, 150);
        assertThrows(DomainException.class, () -> route.updateRoute(null, null, -10));
        assertThrows(DomainException.class, () -> route.updateRoute(null, null, 0));
    }
}
