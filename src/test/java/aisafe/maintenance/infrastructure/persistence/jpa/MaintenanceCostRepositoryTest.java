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
class MaintenanceCostRepositoryTest {

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

        recordRepository.save(new MaintenanceRecordJpaEntity(
                UUID.randomUUID(), "Engine check", LocalDateTime.now(), 4, null,
                List.of(part), template, MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.ENGINE), "CS-TPA",
                BigDecimal.valueOf(1000)));
        recordRepository.save(new MaintenanceRecordJpaEntity(
                UUID.randomUUID(), "Airframe check", LocalDateTime.now(), 8, null,
                List.of(part), template, MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.AIRFRAME), "CS-TPA",
                BigDecimal.valueOf(500)));
        recordRepository.save(new MaintenanceRecordJpaEntity(
                UUID.randomUUID(), "Avionics check", LocalDateTime.now(), 6, null,
                List.of(part), template, MaintenanceStatus.PLANNED, Set.of(MaintenanceComponent.AVIONICS), "CS-LXA",
                BigDecimal.valueOf(750)));
    }

    @Test
    void ensureSumCostByAircraftRegistrationReturnsTotalForThatAircraft() {
        BigDecimal total = recordRepository.sumCostByAircraftRegistration("CS-TPA");

        assertNotNull(total);
        assertEquals(0, BigDecimal.valueOf(1500).compareTo(total));
    }

    @Test
    void ensureSumCostByAircraftRegistrationReturnsNullWhenNoRecords() {
        BigDecimal total = recordRepository.sumCostByAircraftRegistration("CS-UNKNOWN");

        assertNull(total);
    }

    @Test
    void ensureSumCostByRegistrationsReturnsTotalAcrossAllProvided() {
        BigDecimal total = recordRepository.sumCostByRegistrations(List.of("CS-TPA", "CS-LXA"));

        assertNotNull(total);
        assertEquals(0, BigDecimal.valueOf(2250).compareTo(total));
    }

    @Test
    void ensureSumCostByRegistrationsReturnsTotalForSubset() {
        BigDecimal total = recordRepository.sumCostByRegistrations(List.of("CS-LXA"));

        assertNotNull(total);
        assertEquals(0, BigDecimal.valueOf(750).compareTo(total));
    }

    @Test
    void ensureSumCostByRegistrationsReturnsNullWhenNoneMatch() {
        BigDecimal total = recordRepository.sumCostByRegistrations(List.of("CS-UNKNOWN"));

        assertNull(total);
    }
}
