package aisafe.maintenance.domain;

import aisafe.aircrafts.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
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
                List.of(new ModelName("A320")), List.of("Check engine"), 500, 365);
    }

    @Test
    void ensureValidRecordIsCreated() {
        MaintenanceRecord record = new MaintenanceRecord(
                UUID.randomUUID(), "Engine inspection", LocalDateTime.now(), 4,
                List.of(buildPart()), "Some notes", buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA"), BigDecimal.valueOf(1000), null);
        assertEquals(MaintenanceStatus.PLANNED, record.getStatus());
        assertEquals("Engine inspection", record.getDescription());
        assertEquals(4, record.getExpectedDuration());
    }

    @Test
    void ensureBlankDescriptionThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceRecord(UUID.randomUUID(), "  ", LocalDateTime.now(), 4,
                        List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA"), BigDecimal.valueOf(1000), null));
    }

    @Test
    void ensureNullDescriptionThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceRecord(UUID.randomUUID(), null, LocalDateTime.now(), 4,
                        List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA"), BigDecimal.valueOf(1000), null));
    }

    @Test
    void ensureNullStartDateThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceRecord(UUID.randomUUID(), "Engine check", null, 4,
                        List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA"), BigDecimal.valueOf(1000), null));
    }

    @Test
    void ensureNullExpectedDurationThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), null,
                        List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA"), BigDecimal.valueOf(1000), null));
    }

    @Test
    void ensureZeroExpectedDurationThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), 0,
                        List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA"), BigDecimal.valueOf(1000), null));
    }

    @Test
    void ensureNullPartsThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4,
                        null, null, buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA"), BigDecimal.valueOf(1000), null));
    }

    @Test
    void ensureEmptyPartsThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4,
                        List.of(), null, buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA"), BigDecimal.valueOf(1000), null));
    }

    @Test
    void ensureNullAircraftThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4,
                        List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), null, BigDecimal.valueOf(1000), null));
    }

    @Test
    void ensureNullTemplateThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4,
                        List.of(buildPart()), null, null, MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA"), BigDecimal.valueOf(1000), null));
    }

    @Test
    void ensureNullStatusThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4,
                        List.of(buildPart()), null, buildTemplate(), null, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA"), BigDecimal.valueOf(1000), null));
    }

    @Test
    void ensureNotesAreOptional() {
        assertDoesNotThrow(() ->
                new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4,
                        List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA"), BigDecimal.valueOf(1000), null));
    }

    @Test
    void ensureNullComponentsThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4,
                        List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, null, new RegistrationNumber("CS-TPA"), BigDecimal.valueOf(1000), null));
    }

    @Test
    void ensureEmptyComponentsThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4,
                        List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, Set.of(), new RegistrationNumber("CS-TPA"), BigDecimal.valueOf(1000), null));
    }

    @Test
    void ensureNullCostThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4,
                        List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA"), null, null));
    }

    @Test
    void ensureNegativeCostThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4,
                        List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA"), BigDecimal.valueOf(-1), null));
    }

    @Test
    void ensureComponentsAreImmutable() {
        var input = new HashSet<>(Set.of(MaintenanceComponent.ENGINE));
        MaintenanceRecord record = new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4,
                List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, input, new RegistrationNumber("CS-TPA"), BigDecimal.valueOf(1000), null);
        input.add(MaintenanceComponent.AVIONICS);
        assertEquals(1, record.getComponents().size());
    }

    @Test
    void ensureCompletedAtIsSetWhenStatusBecomesCompleted() {
        MaintenanceRecord record = new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4,
                List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE),
                new RegistrationNumber("CS-TPA"), BigDecimal.valueOf(1000), null);

        record.changeStatus(MaintenanceStatus.COMPLETED);

        assertNotNull(record.getCompletedAt());
        assertTrue(record.getCompletedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void ensureCompletedAtIsNotResetWhenSettingCompletedAgain() {
        LocalDateTime known = LocalDateTime.of(2024, 6, 1, 10, 0);
        MaintenanceRecord record = new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4,
                List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.COMPLETED, Set.of(MaintenanceComponent.ENGINE),
                new RegistrationNumber("CS-TPA"), BigDecimal.valueOf(1000), known);

        record.changeStatus(MaintenanceStatus.COMPLETED);

        assertEquals(known, record.getCompletedAt());
    }

    @Test
    void ensureCompletedAtFromConstructorIsPreservedOnReconstitution() {
        LocalDateTime known = LocalDateTime.of(2024, 5, 15, 8, 30);
        MaintenanceRecord record = new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4,
                List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.COMPLETED, Set.of(MaintenanceComponent.ENGINE),
                new RegistrationNumber("CS-TPA"), BigDecimal.valueOf(1000), known);

        assertEquals(known, record.getCompletedAt());
     }

    @Test
    void ensureCompletedAtIsNotSetWhenStatusChangedToInProgress() {
        MaintenanceRecord record = new MaintenanceRecord(UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4,
                List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE),
                new RegistrationNumber("CS-TPA"), BigDecimal.valueOf(1000), null);

        record.changeStatus(MaintenanceStatus.IN_PROGRESS);

        assertNull(record.getCompletedAt());
        assertEquals(MaintenanceStatus.IN_PROGRESS, record.getStatus());
    }

    @Test
    void ensureNullRecordIdThrowsException() {
        assertThrows(MaintenanceInvalidFieldException.class, () ->
                new MaintenanceRecord(null, "Engine check", LocalDateTime.now(), 4,
                        List.of(buildPart()), null, buildTemplate(), MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE),
                        new RegistrationNumber("CS-TPA"), BigDecimal.valueOf(1000), null));
    }
}
