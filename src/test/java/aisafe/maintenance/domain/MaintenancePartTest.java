package aisafe.maintenance.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaintenancePartTest {

    @Test
    void ensureValidPartIsCreated() {
        MaintenancePart part = new MaintenancePart("P001", "Engine Filter", "Filters debris", 10, 2, MaintenanceComponent.ENGINE);
        assertEquals("P001", part.getPartNumber());
    }

    @Test
    void ensureBlankNameThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenancePart("P001", "  ", null, 10, 2, MaintenanceComponent.ENGINE));
    }

    @Test
    void ensureNullPartNumberThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenancePart(null, "Engine Filter", null, 10, 2, MaintenanceComponent.ENGINE));
    }

    @Test
    void ensureNullStockQuantityThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenancePart("P001", "Engine Filter", null, null, 2, MaintenanceComponent.ENGINE));
    }

    @Test
    void ensureNullMinimumThresholdThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenancePart("P001", "Engine Filter", null, 10, null, MaintenanceComponent.ENGINE));
    }

    @Test
    void ensureNullComponentThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenancePart("P001", "Engine Filter", null, 10, 2, null));
    }

    @Test
    void ensureNegativeStockQuantityThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenancePart("P001", "Engine Filter", null, -1, 2, MaintenanceComponent.ENGINE));
    }

    @Test
    void ensureNegativeMinimumThresholdThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenancePart("P001", "Engine Filter", null, 10, -1, MaintenanceComponent.ENGINE));
    }

    @Test
    void ensureZeroStockAndThresholdAreAccepted() {
        assertDoesNotThrow(() ->
                new MaintenancePart("P001", "Engine Filter", null, 0, 0, MaintenanceComponent.ENGINE));
    }
}
