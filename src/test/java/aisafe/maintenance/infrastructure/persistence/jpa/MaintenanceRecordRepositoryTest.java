package aisafe.maintenance.infrastructure.persistence.jpa;

import aisafe.maintenance.domain.MaintenanceComponent;
import aisafe.maintenance.domain.MaintenanceStatus;
import aisafe.maintenance.domain.MaintenanceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("jpa")
class MaintenanceRecordRepositoryTest {

    @Autowired
    private SpringDataMaintenanceRecordRepository recordRepository;

    @Autowired
    private SpringDataMaintenancePartRepository partRepository;

    @Autowired
    private SpringDataMaintenanceTemplateRepository templateRepository;

    private MaintenancePartJpaEntity part;
    private MaintenanceTemplateJpaEntity template;

    @BeforeEach
    void setUp() {
        part = new MaintenancePartJpaEntity("PN-001", "Filter", "A filter", 10, 2, MaintenanceComponent.ENGINE);
        partRepository.save(part);

        template = new MaintenanceTemplateJpaEntity("Annual", MaintenanceType.INSPECTION, List.of("A320"), List.of("Check"), 500, 365);
        templateRepository.save(template);
    }

    @Test
    void ensureRecordCanBeSavedAndFoundByRecordId() {
        UUID recordId = UUID.randomUUID();
        MaintenanceRecordJpaEntity record = new MaintenanceRecordJpaEntity(
                recordId, "Oil change", LocalDateTime.now(), 4, "notes",
                List.of(part), template, MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE),
                new RegistrationNumberJpaEmbeddable("CS-TPA"), BigDecimal.valueOf(100));
        recordRepository.save(record);

        var found = recordRepository.findByRecordId(recordId);
        assertTrue(found.isPresent());
        assertEquals("Oil change", found.get().getDescription());
        assertEquals("CS-TPA", found.get().getAircraftRegistration().getNumber());
    }

    @Test
    void ensureSumTotalExpectedDurationWorks() {
        recordRepository.save(new MaintenanceRecordJpaEntity(UUID.randomUUID(), "R1", LocalDateTime.now(), 5, null, List.of(part), template, MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumberJpaEmbeddable("CS-A"), BigDecimal.valueOf(100)));
        recordRepository.save(new MaintenanceRecordJpaEntity(UUID.randomUUID(), "R2", LocalDateTime.now(), 10, null, List.of(part), template, MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumberJpaEmbeddable("CS-B"), BigDecimal.valueOf(200)));

        Long total = recordRepository.sumTotalExpectedDuration();
        assertEquals(15L, total);
    }

    @Test
    void ensureExistsByAircraftRegistrationWorks() {
        recordRepository.save(new MaintenanceRecordJpaEntity(UUID.randomUUID(), "R1", LocalDateTime.now(), 5, null, List.of(part), template, MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), new RegistrationNumberJpaEmbeddable("CS-TPA"), BigDecimal.valueOf(100)));

        assertTrue(recordRepository.existsByAircraftRegistration_Number("CS-TPA"));
        assertFalse(recordRepository.existsByAircraftRegistration_Number("UNKNOWN"));
    }

    @Test
    void ensureFindByAircraftRegistrationAndStatusOrderByCompletedAtDescWorks() {
        UUID id1 = UUID.randomUUID();
        MaintenanceRecordJpaEntity record1 = new MaintenanceRecordJpaEntity(
                id1, "R1", LocalDateTime.now(), 5, null, List.of(part), template, MaintenanceStatus.COMPLETED, Set.of(MaintenanceComponent.ENGINE),
                new RegistrationNumberJpaEmbeddable("CS-TPA"), BigDecimal.valueOf(100));
        record1.setCompletedAt(LocalDateTime.now().minusDays(2));
        recordRepository.save(record1);

        UUID id2 = UUID.randomUUID();
        MaintenanceRecordJpaEntity record2 = new MaintenanceRecordJpaEntity(
                id2, "R2", LocalDateTime.now(), 5, null, List.of(part), template, MaintenanceStatus.COMPLETED, Set.of(MaintenanceComponent.ENGINE),
                new RegistrationNumberJpaEmbeddable("CS-TPA"), BigDecimal.valueOf(100));
        record2.setCompletedAt(LocalDateTime.now().minusDays(1));
        recordRepository.save(record2);

        var list = recordRepository.findByAircraftRegistration_NumberAndStatusOrderByCompletedAtDesc("CS-TPA", MaintenanceStatus.COMPLETED);
        assertEquals(2, list.size());
        assertEquals(id2, list.get(0).getRecordId());
    }
}
