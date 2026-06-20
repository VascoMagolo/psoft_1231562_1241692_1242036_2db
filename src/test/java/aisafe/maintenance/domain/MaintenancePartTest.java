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

    @Test
    void ensureInvariantsRemainImmutableDuringUpdate() {
        MaintenancePart part = new MaintenancePart("P001", "Engine Filter", "Old desc", 10, 2, MaintenanceComponent.ENGINE);
        
        part.updateDetails("New desc", 20, 5);
        
        assertEquals("P001", part.getPartNumber());
        assertEquals("Engine Filter", part.getName());
        assertEquals(MaintenanceComponent.ENGINE, part.getComponent());
        assertEquals("New desc", part.getDescription());
    }

    @Test
    void ensureEqualsAndHashCodeWorkBasedOnIdentity() {
        MaintenancePart part1 = new MaintenancePart("P001", "Engine Filter", null, 10, 2, MaintenanceComponent.ENGINE);
        MaintenancePart part2 = new MaintenancePart("P001", "Different Name", "Different desc", 5, 1, MaintenanceComponent.ENGINE);
        MaintenancePart part3 = new MaintenancePart("P002", "Engine Filter", null, 10, 2, MaintenanceComponent.ENGINE);

        assertEquals(part1, part2);
        assertNotEquals(part1, part3);
        assertEquals(part1.hashCode(), part2.hashCode());
        assertNotEquals(part1.hashCode(), part3.hashCode());
    }
}
