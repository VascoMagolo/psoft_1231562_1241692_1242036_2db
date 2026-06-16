package aisafe.maintenance.domain;

import aisafe.aircrafts.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
                UUID.randomUUID(), "Engine inspection", LocalDateTime.now(), 4,
                List.of(buildPart()), "Some notes", buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA"));
        assertEquals(MaintenanceStatus.PLANNED, record.getStatus());
        assertEquals("Engine inspection", record.getDescription());
        assertEquals(4, record.getExpectedDuration());
    }

    @Test
    void ensureBlankDescriptionThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceRecord(UUID.randomUUID(), "  ", LocalDateTime.now(), 4,
                        List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA")));
    }

    @Test
    void ensureNullStartDateThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceRecord(UUID.randomUUID(), "Engine check", null, 4,
                        List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA")));
    }

    @Test
    void ensureNullExpectedDurationThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), null,
                        List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA")));
    }

    @Test
    void ensureZeroExpectedDurationThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), 0,
                        List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA")));
    }

    @Test
    void ensureNullPartsThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4,
                        null, null, buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA")));
    }

    @Test
    void ensureEmptyPartsThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4,
                        List.of(), null, buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA")));
    }

    @Test
    void ensureNullAircraftThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4,
                        List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), null));
    }

    @Test
    void ensureNullTemplateThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4,
                        List.of(buildPart()), null, null, MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA")));
    }

    @Test
    void ensureNullStatusThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4,
                        List.of(buildPart()), null, buildTemplate(), null, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA")));
    }

    @Test
    void ensureNotesAreOptional() {
        assertDoesNotThrow(() ->
                new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4,
                        List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA")));
    }

    @Test
    void ensureNullComponentsThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4,
                        List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, null, new RegistrationNumber("CS-TPA")));
    }

    @Test
    void ensureEmptyComponentsThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4,
                        List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, Set.of(), new RegistrationNumber("CS-TPA")));
    }

    @Test
    void ensureComponentsAreImmutable() {
        var input = new HashSet<>(Set.of(MaintenanceComponent.ENGINE));
        MaintenanceRecord record = new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4,
                List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, input, new RegistrationNumber("CS-TPA"));
        input.add(MaintenanceComponent.AVIONICS);
        assertEquals(1, record.getComponents().size());
    }
}
