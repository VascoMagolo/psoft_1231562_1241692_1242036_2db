package aisafe.maintenance.domain;

import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.Manufacturer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MaintenanceTemplateTest {

    private AircraftModel buildModel() {
        return new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "a320.jpg", 180);
    }

    @Test
    void ensureValidTemplateIsCreated() {
        MaintenanceTemplate template = new MaintenanceTemplate(
                "Annual Inspection", MaintenanceType.INSPECTION,
                List.of(buildModel()), List.of("Check engine", "Check avionics"), 500, 365);
        assertEquals("Annual Inspection", template.getName());
    }

    @Test
    void ensureNullNameThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new MaintenanceTemplate(null, MaintenanceType.INSPECTION, List.of(buildModel()), List.of("Check"), 500, 365));
    }

    @Test
    void ensureBlankNameThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new MaintenanceTemplate("  ", MaintenanceType.INSPECTION, List.of(buildModel()), List.of("Check"), 500, 365));
    }

    @Test
    void ensureNullTemplateTypeThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new MaintenanceTemplate("Annual Inspection", null, List.of(buildModel()), List.of("Check"), 500, 365));
    }

    @Test
    void ensureNullApplicableModelsThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new MaintenanceTemplate("Annual Inspection", MaintenanceType.INSPECTION, null, List.of("Check"), 500, 365));
    }

    @Test
    void ensureNullChecklistThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new MaintenanceTemplate("Annual Inspection", MaintenanceType.INSPECTION, List.of(buildModel()), null, 500, 365));
    }

    @Test
    void ensureNullIntervalFlightHoursThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new MaintenanceTemplate("Annual Inspection", MaintenanceType.INSPECTION, List.of(buildModel()), List.of("Check"), null, 365));
    }

    @Test
    void ensureNullIntervalDaysThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new MaintenanceTemplate("Annual Inspection", MaintenanceType.INSPECTION, List.of(buildModel()), List.of("Check"), 500, null));
    }
}
