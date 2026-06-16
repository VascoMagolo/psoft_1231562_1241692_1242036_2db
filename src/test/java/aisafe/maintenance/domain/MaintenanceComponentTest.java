package aisafe.maintenance.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MaintenanceComponentTest {

    @Test
    void ensureAllEnumConstantsExist() {
        assertNotNull(MaintenanceComponent.valueOf("ENGINE"));
        assertNotNull(MaintenanceComponent.valueOf("AIRFRAME"));
        assertNotNull(MaintenanceComponent.valueOf("AVIONICS"));
        assertNotNull(MaintenanceComponent.valueOf("INTERIOR"));
        assertNotNull(MaintenanceComponent.valueOf("EXTERIOR"));
    }

    @Test
    void ensureEnumSize() {
        assertEquals(5, MaintenanceComponent.values().length);
    }
}
