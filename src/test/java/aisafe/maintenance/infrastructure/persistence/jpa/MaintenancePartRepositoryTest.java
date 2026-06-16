package aisafe.maintenance.infrastructure.persistence.jpa;

import aisafe.maintenance.domain.MaintenanceComponent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("jpa")
class MaintenancePartRepositoryTest {

    @Autowired
    private SpringDataMaintenancePartRepository repository;

    @Test
    void ensurePartCanBeSavedAndFoundByPartNumber() {
        MaintenancePartJpaEntity part = new MaintenancePartJpaEntity("PN-001", "Filter", "A filter", 10, 2, MaintenanceComponent.ENGINE);
        repository.save(part);

        var found = repository.findByPartNumber("PN-001");
        assertTrue(found.isPresent());
        assertEquals("Filter", found.get().getName());
    }

    @Test
    void ensureExistsByPartNumberWorks() {
        MaintenancePartJpaEntity part = new MaintenancePartJpaEntity("PN-002", "Bolt", "A bolt", 50, 10, MaintenanceComponent.AIRFRAME);
        repository.save(part);

        assertTrue(repository.existsByPartNumber("PN-002"));
        assertFalse(repository.existsByPartNumber("NON-EXISTENT"));
    }
}
