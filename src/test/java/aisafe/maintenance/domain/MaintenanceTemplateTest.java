package aisafe.maintenance.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MaintenanceTemplateTest {

    @Test
    void ensureValidTemplateIsCreated() {
        MaintenanceTemplate template = new MaintenanceTemplate(
                "Annual Inspection", MaintenanceType.INSPECTION,
                List.of("A320"), List.of("Check engine", "Check avionics"), 500, 365);
        assertEquals("Annual Inspection", template.getName());
    }

    @Test
    void ensureNullNameThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceTemplate(null, MaintenanceType.INSPECTION, List.of("A320"), List.of("Check"), 500, 365));
    }

    @Test
    void ensureBlankNameThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceTemplate("  ", MaintenanceType.INSPECTION, List.of("A320"), List.of("Check"), 500, 365));
    }

    @Test
    void ensureNullTemplateTypeThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceTemplate("Annual Inspection", null, List.of("A320"), List.of("Check"), 500, 365));
    }

    @Test
    void ensureNullApplicableModelsThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceTemplate("Annual Inspection", MaintenanceType.INSPECTION, null, List.of("Check"), 500, 365));
    }

    @Test
    void ensureNullChecklistThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceTemplate("Annual Inspection", MaintenanceType.INSPECTION, List.of("A320"), null, 500, 365));
    }

    @Test
    void ensureNullIntervalFlightHoursThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceTemplate("Annual Inspection", MaintenanceType.INSPECTION, List.of("A320"), List.of("Check"), null, 365));
    }

    @Test
    void ensureNullIntervalDaysThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceTemplate("Annual Inspection", MaintenanceType.INSPECTION, List.of("A320"), List.of("Check"), 500, null));
    }
}
