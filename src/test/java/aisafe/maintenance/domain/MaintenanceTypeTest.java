package aisafe.maintenance.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MaintenanceTypeTest {

    @Test
    void ensureAllEnumConstantsExist() {
        assertNotNull(MaintenanceType.valueOf("INSPECTION"));
        assertNotNull(MaintenanceType.valueOf("SCHEDULED_MAINTENANCE"));
        assertNotNull(MaintenanceType.valueOf("OVERHAUL"));
        assertNotNull(MaintenanceType.valueOf("MODIFICATION"));
    }

    @Test
    void ensureEnumSize() {
        assertEquals(4, MaintenanceType.values().length);
    }
}
