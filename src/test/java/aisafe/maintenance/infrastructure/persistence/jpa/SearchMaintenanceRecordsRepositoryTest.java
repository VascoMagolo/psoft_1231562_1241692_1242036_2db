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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("jpa")
class SearchMaintenanceRecordsRepositoryTest {

    @Autowired
    private SpringDataMaintenanceRecordRepository recordRepository;

    @Autowired
    private SpringDataMaintenancePartRepository partRepository;

    @Autowired
    private SpringDataMaintenanceTemplateRepository templateRepository;

    private MaintenancePartJpaEntity part;
    private MaintenanceTemplateJpaEntity template;

    private final LocalDateTime date2026Jan = LocalDateTime.of(2026, 1, 15, 10, 0);
    private final LocalDateTime date2026Jun = LocalDateTime.of(2026, 6, 15, 10, 0);
    private final LocalDateTime date2025Dec = LocalDateTime.of(2025, 12, 1, 10, 0);

    @BeforeEach
    void setUp() {
        part = partRepository.save(
                new MaintenancePartJpaEntity("PN-001", "Filter", "A filter", 10, 2, MaintenanceComponent.ENGINE));
        template = templateRepository.save(
                new MaintenanceTemplateJpaEntity("Annual", MaintenanceType.INSPECTION, List.of("A320"), List.of("Check"), 500, 365));

        recordRepository.save(new MaintenanceRecordJpaEntity(
                UUID.randomUUID(), "Engine check", date2026Jan, 4, null,
                List.of(part), template, MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE),
                new RegistrationNumberJpaEmbeddable("CS-TPA"), BigDecimal.valueOf(100)));
        recordRepository.save(new MaintenanceRecordJpaEntity(
                UUID.randomUUID(), "Airframe inspection", date2026Jun, 8, null,
                List.of(part), template, MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.AIRFRAME),
                new RegistrationNumberJpaEmbeddable("CS-TPA"), BigDecimal.valueOf(200)));
        recordRepository.save(new MaintenanceRecordJpaEntity(
                UUID.randomUUID(), "Avionics check", date2025Dec, 6, null,
                List.of(part), template, MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.AVIONICS),
                new RegistrationNumberJpaEmbeddable("CS-LXA"), BigDecimal.valueOf(150)));
    }

    @Test
    void ensureSearchWithNoFiltersReturnsAllRecords() {
        Page<MaintenanceRecordJpaEntity> page = recordRepository.search(null, null, null, null, PageRequest.of(0, 20));
        assertEquals(3, page.getTotalElements());
    }

    @Test
    void ensureSearchByAircraftRegistrationFilters() {
        Page<MaintenanceRecordJpaEntity> page = recordRepository.search("CS-TPA", null, null, null, PageRequest.of(0, 20));
        assertEquals(2, page.getTotalElements());
    }

    @Test
    void ensureSearchByDateRangeFilters() {
        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 6, 30, 23, 59);
        Page<MaintenanceRecordJpaEntity> page = recordRepository.search(null, from, to, null, PageRequest.of(0, 20));
        assertEquals(2, page.getTotalElements());
    }

    @Test
    void ensureSearchByComponentFilters() {
        Page<MaintenanceRecordJpaEntity> page = recordRepository.search(null, null, null, MaintenanceComponent.ENGINE, PageRequest.of(0, 20));
        assertEquals(1, page.getTotalElements());
        assertEquals("CS-TPA", page.getContent().get(0).getAircraftRegistration().getNumber());
    }

    @Test
    void ensureSearchWithAllFiltersReturnsOnlyMatchingRecord() {
        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 3, 31, 23, 59);
        Page<MaintenanceRecordJpaEntity> page = recordRepository.search(
                "CS-TPA", from, to, MaintenanceComponent.ENGINE, PageRequest.of(0, 20));
        assertEquals(1, page.getTotalElements());
        assertEquals("Engine check", page.getContent().get(0).getDescription());
    }

    @Test
    void ensureSearchDoesNotReturnDuplicatesWhenRecordHasMultipleComponents() {
        recordRepository.save(new MaintenanceRecordJpaEntity(
                UUID.randomUUID(), "Multi-component check", date2026Jan, 3, null,
                List.of(part), template, MaintenanceStatus.PLANNED,
                Set.of(MaintenanceComponent.ENGINE, MaintenanceComponent.AVIONICS, MaintenanceComponent.AIRFRAME),
                new RegistrationNumberJpaEmbeddable("CS-DUP"), BigDecimal.valueOf(100)));

        Page<MaintenanceRecordJpaEntity> page = recordRepository.search("CS-DUP", null, null, null, PageRequest.of(0, 20));
        assertEquals(1, page.getTotalElements());
        assertEquals(1, page.getContent().size());
    }

    @Test
    void ensureSearchReturnsEmptyPageWhenNoMatch() {
        Page<MaintenanceRecordJpaEntity> page = recordRepository.search("CS-XXX", null, null, null, PageRequest.of(0, 20));
        assertEquals(0, page.getTotalElements());
    }
}
