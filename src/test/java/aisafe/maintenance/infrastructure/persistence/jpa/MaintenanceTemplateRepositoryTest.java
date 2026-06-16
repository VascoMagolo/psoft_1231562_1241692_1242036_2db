package aisafe.maintenance.infrastructure.persistence.jpa;

import aisafe.maintenance.domain.MaintenanceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("jpa")
class MaintenanceTemplateRepositoryTest {

    @Autowired
    private SpringDataMaintenanceTemplateRepository repository;

    @Test
    void ensureTemplateCanBeSavedAndFoundByName() {
        MaintenanceTemplateJpaEntity template = new MaintenanceTemplateJpaEntity(
                "Annual", MaintenanceType.INSPECTION, List.of("A320"), List.of("Check"), 500, 365);
        repository.save(template);

        var found = repository.findByName("Annual");
        assertTrue(found.isPresent());
        assertEquals(MaintenanceType.INSPECTION, found.get().getTemplateType());
    }

    @Test
    void ensureExistsByNameWorks() {
        MaintenanceTemplateJpaEntity template = new MaintenanceTemplateJpaEntity(
                "Overhaul", MaintenanceType.OVERHAUL, List.of("B737"), List.of("Deep check"), 2000, 1000);
        repository.save(template);

        assertTrue(repository.existsByName("Overhaul"));
        assertFalse(repository.existsByName("NON-EXISTENT"));
    }
}
