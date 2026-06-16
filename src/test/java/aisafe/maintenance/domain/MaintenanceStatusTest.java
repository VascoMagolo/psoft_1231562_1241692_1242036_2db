package aisafe.maintenance.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MaintenanceStatusTest {

    @Test
    void ensureAllEnumConstantsExist() {
        assertNotNull(MaintenanceStatus.valueOf("PLANNED"));
        assertNotNull(MaintenanceStatus.valueOf("IN_PROGRESS"));
        assertNotNull(MaintenanceStatus.valueOf("COMPLETED"));
        assertNotNull(MaintenanceStatus.valueOf("CANCELED"));
    }

    @Test
    void ensureEnumSize() {
        assertEquals(4, MaintenanceStatus.values().length);
    }
}
