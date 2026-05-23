package aisafe.maintenance.domain;

import aisafe.aircrafts.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MaintenanceRecordTest {

    private AircraftModel buildModel() {
        return new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "a320.jpg", 180);
    }

    private MaintenancePart buildPart() {
        return new MaintenancePart("P001", "Engine Filter", null, 10, 2, MaintenanceComponent.ENGINE);
    }

    private MaintenanceTemplate buildTemplate() {
        return new MaintenanceTemplate("Annual Check", MaintenanceType.INSPECTION,
                List.of(buildModel()), List.of("Check engine"), 500, 365);
    }

    private Aircraft buildAircraft() {
        return new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1),
                buildModel(), new RegistrationNumber("CS-TPA"), 150, List.of());
    }

    @Test
    void ensureValidRecordIsCreated() {
        MaintenanceRecord record = new MaintenanceRecord(
                "Engine inspection", LocalDateTime.now(), 4,
                buildPart(), "Some notes", buildTemplate(), MaintenanceStatus.PLANNED, buildAircraft());
        assertEquals(MaintenanceStatus.PLANNED, record.getStatus());
        assertEquals("Engine inspection", record.getDescription());
        assertEquals(4, record.getExpectedDuration());
    }

    @Test
    void ensureBlankDescriptionThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new MaintenanceRecord("  ", LocalDateTime.now(), 4,
                        buildPart(), null, buildTemplate(), MaintenanceStatus.PLANNED, buildAircraft()));
    }

    @Test
    void ensureNullStartDateThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new MaintenanceRecord("Engine check", null, 4,
                        buildPart(), null, buildTemplate(), MaintenanceStatus.PLANNED, buildAircraft()));
    }

    @Test
    void ensureNullExpectedDurationThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new MaintenanceRecord("Engine check", LocalDateTime.now(), null,
                        buildPart(), null, buildTemplate(), MaintenanceStatus.PLANNED, buildAircraft()));
    }

    @Test
    void ensureZeroExpectedDurationThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new MaintenanceRecord("Engine check", LocalDateTime.now(), 0,
                        buildPart(), null, buildTemplate(), MaintenanceStatus.PLANNED, buildAircraft()));
    }

    @Test
    void ensureNullPartThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new MaintenanceRecord("Engine check", LocalDateTime.now(), 4,
                        null, null, buildTemplate(), MaintenanceStatus.PLANNED, buildAircraft()));
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
                        buildPart(), null, null, MaintenanceStatus.PLANNED, buildAircraft()));
    }

    @Test
    void ensureNullStatusThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new MaintenanceRecord("Engine check", LocalDateTime.now(), 4,
                        buildPart(), null, buildTemplate(), null, buildAircraft()));
    }

    @Test
    void ensureNotesAreOptional() {
        assertDoesNotThrow(() ->
                new MaintenanceRecord("Engine check", LocalDateTime.now(), 4,
                        buildPart(), null, buildTemplate(), MaintenanceStatus.PLANNED, buildAircraft()));
    }
}
