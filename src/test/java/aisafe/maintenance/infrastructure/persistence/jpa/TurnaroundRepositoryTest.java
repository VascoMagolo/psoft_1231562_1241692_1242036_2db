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
class TurnaroundRepositoryTest {

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
        part = partRepository.save(
                new MaintenancePartJpaEntity("PN-001", "Filter", "A filter", 10, 2, MaintenanceComponent.ENGINE));
        template = templateRepository.save(
                new MaintenanceTemplateJpaEntity("Annual", MaintenanceType.INSPECTION, List.of("A320"), List.of("Check"), 500, 365));

        MaintenanceRecordJpaEntity r1 = new MaintenanceRecordJpaEntity(
                UUID.randomUUID(), "Completed check", LocalDateTime.of(2024, 6, 1, 8, 0), 32, null,
                List.of(part), template, MaintenanceStatus.COMPLETED, Set.of(MaintenanceComponent.ENGINE),
                new RegistrationNumberJpaEmbeddable("CS-TPA"), BigDecimal.valueOf(1000));
        r1.setCompletedAt(LocalDateTime.of(2024, 6, 2, 16, 0));
        recordRepository.save(r1);

        MaintenanceRecordJpaEntity r2 = new MaintenanceRecordJpaEntity(
                UUID.randomUUID(), "Another completed", LocalDateTime.of(2024, 7, 1, 8, 0), 24, null,
                List.of(part), template, MaintenanceStatus.COMPLETED, Set.of(MaintenanceComponent.ENGINE),
                new RegistrationNumberJpaEmbeddable("CS-TPA"), BigDecimal.valueOf(500));
        r2.setCompletedAt(LocalDateTime.of(2024, 7, 2, 8, 0));
        recordRepository.save(r2);

        recordRepository.save(new MaintenanceRecordJpaEntity(
                UUID.randomUUID(), "Ongoing check", LocalDateTime.now(), 8, null,
                List.of(part), template, MaintenanceStatus.IN_PROGRESS, Set.of(MaintenanceComponent.ENGINE),
                new RegistrationNumberJpaEmbeddable("CS-TPA"), BigDecimal.valueOf(200)));
    }

    @Test
    void ensureAverageTurnaroundExcludesNonCompletedRecords() {
        Double avg = recordRepository.findAverageTurnaroundHours(List.of("CS-TPA"));

        assertNotNull(avg);
        assertEquals(28.0, avg, 0.001);
    }

    @Test
    void ensureAverageTurnaroundReturnsNullWhenNoCompletedRecordsForRegistration() {
        Double avg = recordRepository.findAverageTurnaroundHours(List.of("CS-OTHER"));

        assertNull(avg);
    }
}
