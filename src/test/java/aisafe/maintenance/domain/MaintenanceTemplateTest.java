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

    @Test
    void ensureInvariantsRemainImmutableDuringUpdate() {
        MaintenanceTemplate template = new MaintenanceTemplate("T1", MaintenanceType.INSPECTION, List.of("M1"), List.of("C1"), 100, 30);
        
        template.updateDetails(List.of("C2"), 200, 60);
        
        assertEquals("T1", template.getName());
        assertEquals(MaintenanceType.INSPECTION, template.getTemplateType());
        assertEquals(List.of("M1"), template.getApplicableModelNames());
        assertEquals(List.of("C2"), template.getChecklist());
    }

    @Test
    void ensureEqualsAndHashCodeWorkBasedOnIdentity() {
        MaintenanceTemplate temp1 = new MaintenanceTemplate("T1", MaintenanceType.INSPECTION, List.of("M1"), List.of("C1"), 100, 30);
        MaintenanceTemplate temp2 = new MaintenanceTemplate("T1", MaintenanceType.OVERHAUL, List.of("M2"), List.of("C2"), 500, 365);
        MaintenanceTemplate temp3 = new MaintenanceTemplate("T2", MaintenanceType.INSPECTION, List.of("M1"), List.of("C1"), 100, 30);

        assertEquals(temp1, temp2);
        assertNotEquals(temp1, temp3);
        assertEquals(temp1.hashCode(), temp2.hashCode());
        assertNotEquals(temp1.hashCode(), temp3.hashCode());
    }
}
