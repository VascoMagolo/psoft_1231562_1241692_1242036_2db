package aisafe.maintenance.infrastructure.persistence.jpa;

import aisafe.maintenance.domain.MaintenanceComponent;
import aisafe.maintenance.domain.MaintenanceStatus;
import aisafe.maintenance.domain.MaintenanceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("jpa")
class OngoingMaintenanceRepositoryTest {

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
    }

    @Test
    void ensureFindByStatusReturnsOnlyInProgressRecords() {
        recordRepository.save(new MaintenanceRecordJpaEntity(
                UUID.randomUUID(), "Engine overhaul", LocalDateTime.of(2026, 6, 1, 8, 0), 8, null,
                List.of(part), template, MaintenanceStatus.IN_PROGRESS, Set.of(MaintenanceComponent.ENGINE), "CS-TPA"));
        recordRepository.save(new MaintenanceRecordJpaEntity(
                UUID.randomUUID(), "Airframe check", LocalDateTime.of(2026, 6, 5, 9, 0), 4, null,
                List.of(part), template, MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.AIRFRAME), "CS-TPA"));
        recordRepository.save(new MaintenanceRecordJpaEntity(
                UUID.randomUUID(), "Avionics check", LocalDateTime.of(2026, 5, 10, 10, 0), 6, null,
                List.of(part), template, MaintenanceStatus.COMPLETED, Set.of(MaintenanceComponent.AVIONICS), "CS-LXA"));

        Page<MaintenanceRecordJpaEntity> page = recordRepository.findByStatusOrderByStartDateDesc(
                MaintenanceStatus.IN_PROGRESS, PageRequest.of(0, 20));

        assertEquals(1, page.getTotalElements());
        assertEquals("Engine overhaul", page.getContent().get(0).getDescription());
    }

    @Test
    void ensureFindByStatusOrderedByStartDateDesc() {
        recordRepository.save(new MaintenanceRecordJpaEntity(
                UUID.randomUUID(), "Earlier record", LocalDateTime.of(2026, 5, 1, 8, 0), 4, null,
                List.of(part), template, MaintenanceStatus.IN_PROGRESS, Set.of(MaintenanceComponent.ENGINE), "CS-TPA"));
        recordRepository.save(new MaintenanceRecordJpaEntity(
                UUID.randomUUID(), "Later record", LocalDateTime.of(2026, 6, 15, 8, 0), 4, null,
                List.of(part), template, MaintenanceStatus.IN_PROGRESS, Set.of(MaintenanceComponent.AIRFRAME), "CS-LXA"));

        Page<MaintenanceRecordJpaEntity> page = recordRepository.findByStatusOrderByStartDateDesc(
                MaintenanceStatus.IN_PROGRESS, PageRequest.of(0, 20));

        assertEquals(2, page.getTotalElements());
        assertEquals("Later record", page.getContent().get(0).getDescription());
        assertEquals("Earlier record", page.getContent().get(1).getDescription());
    }

    @Test
    void ensureFindByStatusReturnsEmptyWhenNoneMatch() {
        recordRepository.save(new MaintenanceRecordJpaEntity(
                UUID.randomUUID(), "Planned only", LocalDateTime.of(2026, 7, 1, 8, 0), 4, null,
                List.of(part), template, MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), "CS-TPA"));

        Page<MaintenanceRecordJpaEntity> page = recordRepository.findByStatusOrderByStartDateDesc(
                MaintenanceStatus.IN_PROGRESS, PageRequest.of(0, 20));

        assertTrue(page.isEmpty());
    }
}
