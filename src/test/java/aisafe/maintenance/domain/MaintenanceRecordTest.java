package aisafe.maintenance.domain;

import aisafe.aircrafts.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MaintenanceRecordTest {

    private MaintenancePart buildPart() {
        return new MaintenancePart("P001", "Engine Filter", null, 10, 2, MaintenanceComponent.ENGINE);
    }

    private MaintenanceTemplate buildTemplate() {
        return new MaintenanceTemplate("Annual Check", MaintenanceType.INSPECTION,
                List.of("A320"), List.of("Check engine"), 500, 365);
    }

    @Test
    void ensureValidRecordIsCreated() {
        MaintenanceRecord record = new MaintenanceRecord(
                "Engine inspection", LocalDateTime.now(), 4,
                buildPart(), "Some notes", buildTemplate(), MaintenanceStatus.PLANNED, "CS-TPA");
        assertEquals(MaintenanceStatus.PLANNED, record.getStatus());
        assertEquals("Engine inspection", record.getDescription());
        assertEquals(4, record.getExpectedDuration());
    }

    @Test
    void ensureBlankDescriptionThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new MaintenanceRecord("  ", LocalDateTime.now(), 4,
                        buildPart(), null, buildTemplate(), MaintenanceStatus.PLANNED, "CS-TPA"));
    }

    @Test
    void ensureNullStartDateThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new MaintenanceRecord("Engine check", null, 4,
                        buildPart(), null, buildTemplate(), MaintenanceStatus.PLANNED, "CS-TPA"));
    }

    @Test
    void ensureNullExpectedDurationThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new MaintenanceRecord("Engine check", LocalDateTime.now(), null,
                        buildPart(), null, buildTemplate(), MaintenanceStatus.PLANNED, "CS-TPA"));
    }

    @Test
    void ensureZeroExpectedDurationThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new MaintenanceRecord("Engine check", LocalDateTime.now(), 0,
                        buildPart(), null, buildTemplate(), MaintenanceStatus.PLANNED, "CS-TPA"));
    }

    @Test
    void ensureNullPartThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new MaintenanceRecord("Engine check", LocalDateTime.now(), 4,
                        null, null, buildTemplate(), MaintenanceStatus.PLANNED, "CS-TPA"));
    }

    @Test
    void ensureNullAircraftThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new MaintenanceRecord("Engine check", LocalDateTime.now(), 4,
                        buildPart(), null, buildTemplate(), MaintenanceStatus.PLANNED, null));
    }

    @Test
    void ensureNullTemplateThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new MaintenanceRecord("Engine check", LocalDateTime.now(), 4,
                        buildPart(), null, null, MaintenanceStatus.PLANNED, "CS-TPA"));
    }

    @Test
    void ensureNullStatusThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new MaintenanceRecord("Engine check", LocalDateTime.now(), 4,
                        buildPart(), null, buildTemplate(), null, "CS-TPA"));
    }

    @Test
    void ensureNotesAreOptional() {
        assertDoesNotThrow(() ->
                new MaintenanceRecord("Engine check", LocalDateTime.now(), 4,
                        buildPart(), null, buildTemplate(), MaintenanceStatus.PLANNED, "CS-TPA"));
    }
}
