package aisafe.routes.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RouteStatusTest {

    @Test
    void ensureAllEnumValuesExist() {
        assertNotNull(RouteStatus.ACTIVE);
        assertNotNull(RouteStatus.INACTIVE);
        assertNotNull(RouteStatus.SUSPENDED);
    }

    @Test
    void ensureEnumValuesToStringAreCorrect() {
        assertEquals("ACTIVE", RouteStatus.ACTIVE.toString());
        assertEquals("INACTIVE", RouteStatus.INACTIVE.toString());
        assertEquals("SUSPENDED", RouteStatus.SUSPENDED.toString());
    }

    @Test
    void ensureValueOfReturnsCorrectEnum() {
        assertEquals(RouteStatus.ACTIVE, RouteStatus.valueOf("ACTIVE"));
        assertEquals(RouteStatus.INACTIVE, RouteStatus.valueOf("INACTIVE"));
        assertEquals(RouteStatus.SUSPENDED, RouteStatus.valueOf("SUSPENDED"));
    }

    @Test
    void ensureValueOfThrowsExceptionForInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> RouteStatus.valueOf("NON_EXISTENT"));
    }
}
