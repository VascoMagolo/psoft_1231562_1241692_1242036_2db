package aisafe.maintenance.infrastructure.persistence.jpa;

import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.aircrafts.domain.ModelName;
import aisafe.maintenance.domain.*;
import aisafe.shared.domain.PaginatedResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("jpa")
@Transactional
class MaintenanceRecordJpaRepositoryAdditionalTest {

    @Autowired
    private MaintenanceRecordRepository repository;

    @Autowired
    private SpringDataMaintenancePartRepository partSpringRepo;

    @Test
    void ensureExistsByStartDateAndTemplateReturnsFalseWhenTemplateNotFound() {
        MaintenanceTemplate nonExistentTemplate = new MaintenanceTemplate(
                "NonExistentName",
                MaintenanceType.INSPECTION,
                List.of(new ModelName("A320")),
                List.of("Check"),
                100,
                10
        );

        assertFalse(repository.existsByStartDateAndTemplate(LocalDateTime.now(), nonExistentTemplate));
    }

    @Test
    void ensureExistsByPartsContainingReturnsFalseWhenPartNotFound() {
        MaintenancePart nonExistentPart = new MaintenancePart(
                "P-NON-EXISTENT",
                "Name",
                "Desc",
                10,
                1,
                MaintenanceComponent.ENGINE
        );

        assertFalse(repository.existsByPartsContaining(nonExistentPart));
    }

    @Test
    void ensureExistsByTemplateReturnsFalseWhenTemplateNotFound() {
        MaintenanceTemplate nonExistentTemplate = new MaintenanceTemplate(
                "NonExistentName",
                MaintenanceType.INSPECTION,
                List.of(new ModelName("A320")),
                List.of("Check"),
                100,
                10
        );

        assertFalse(repository.existsByTemplate(nonExistentTemplate));
    }

    @Test
    void ensureSumCostByRegistrationsReturnsZeroWhenListIsEmpty() {
        assertEquals(BigDecimal.ZERO, repository.sumCostByRegistrations(Collections.emptyList()));
    }

    @Test
    void ensureFindAverageTurnaroundByRegistrationsReturnsZeroWhenListIsEmpty() {
        MaintenanceTurnaroundData result = repository.findAverageTurnaroundByRegistrations("A320", Collections.emptyList());
        assertEquals("A320", result.modelName());
        assertEquals(0.0, result.averageHours());
    }

    @Test
    void ensureSaveThrowsMaintenancePartNotFoundExceptionWhenPartDoesNotExist() {
        MaintenanceTemplate template = new MaintenanceTemplate(
                "Inspection Check",
                MaintenanceType.INSPECTION,
                List.of(new ModelName("A320")),
                List.of("Check"),
                100,
                10
        );

        MaintenancePart nonExistentPart = new MaintenancePart(
                "P-XYZ", "Part", "Desc", 1, 1, MaintenanceComponent.ENGINE
        );

        MaintenanceRecord record = new MaintenanceRecord(
                UUID.randomUUID(),
                "Desc",
                LocalDateTime.now(),
                5,
                List.of(nonExistentPart),
                "Notes",
                template,
                MaintenanceStatus.PLANNED,
                Set.of(MaintenanceComponent.ENGINE),
                new RegistrationNumber("CS-TPA"),
                BigDecimal.valueOf(100.0),
                null
        );

        assertThrows(MaintenancePartNotFoundException.class, () -> repository.save(record));
    }

    @Test
    void ensureSaveThrowsMaintenanceTemplateNotFoundExceptionWhenTemplateDoesNotExist() {
        MaintenancePartJpaEntity partJpa = new MaintenancePartJpaEntity(
                "P-EXISTENT", "Existent Part", "Desc", 10, 2, MaintenanceComponent.ENGINE
        );
        partSpringRepo.save(partJpa);

        MaintenancePart domainPart = new MaintenancePart(
                "P-EXISTENT",
                "Existent Part",
                "Desc",
                10,
                2,
                MaintenanceComponent.ENGINE
        );

        MaintenanceTemplate nonExistentTemplate = new MaintenanceTemplate(
                "NonExistentTemplate",
                MaintenanceType.INSPECTION,
                List.of(new ModelName("A320")),
                List.of("Check"),
                100,
                10
        );

        MaintenanceRecord record = new MaintenanceRecord(
                UUID.randomUUID(),
                "Desc",
                LocalDateTime.now(),
                5,
                List.of(domainPart),
                "Notes",
                nonExistentTemplate,
                MaintenanceStatus.PLANNED,
                Set.of(MaintenanceComponent.ENGINE),
                new RegistrationNumber("CS-TPA"),
                BigDecimal.valueOf(100.0),
                null
        );

        assertThrows(MaintenanceTemplateNotFoundException.class, () -> repository.save(record));
    }

    @Test
    void ensureMaintenanceRecordJpaEntityGettersSettersAndVersion() {
        UUID recordId = UUID.randomUUID();
        LocalDateTime startDate = LocalDateTime.now();
        RegistrationNumberJpaEmbeddable reg = new RegistrationNumberJpaEmbeddable("CS-TPA");
        BigDecimal cost = BigDecimal.valueOf(500.0);

        MaintenanceRecordJpaEntity entity = new MaintenanceRecordJpaEntity(
                recordId, "Description", startDate, 10, "Notes",
                Collections.emptyList(), null, MaintenanceStatus.PLANNED,
                Collections.emptySet(), reg, cost
        );
        entity.setId(123L);
        entity.setVersion(2L);
        entity.setNotes("New Notes");
        entity.setStatus(MaintenanceStatus.COMPLETED);
        entity.setCompletedAt(startDate.plusDays(1));

        assertEquals(123L, entity.getId());
        assertEquals(2L, entity.getVersion());
        assertEquals(recordId, entity.getRecordId());
        assertEquals("Description", entity.getDescription());
        assertEquals(startDate, entity.getStartDate());
        assertEquals(10, entity.getExpectedDuration());
        assertEquals("New Notes", entity.getNotes());
        assertTrue(entity.getParts().isEmpty());
        assertNull(entity.getTemplate());
        assertEquals(MaintenanceStatus.COMPLETED, entity.getStatus());
        assertTrue(entity.getComponents().isEmpty());
        assertEquals("CS-TPA", entity.getAircraftRegistration().getNumber());
        assertEquals(cost, entity.getCost());
        assertEquals(startDate.plusDays(1), entity.getCompletedAt());
    }

    @Autowired
    private SpringDataMaintenanceRecordRepository springRecordRepo;

    @Test
    void ensureNullAggregateResultHandling() {
        springRecordRepo.deleteAll();

        assertEquals(0L, repository.sumTotalMaintenanceHours());
        assertEquals(BigDecimal.ZERO, repository.sumCostByAircraftRegistration(new RegistrationNumber("CS-NON")));
        assertEquals(BigDecimal.ZERO, repository.sumCostByRegistrations(List.of("CS-NON")));

        MaintenanceTurnaroundData turnaround = repository.findAverageTurnaroundByRegistrations("A320", List.of("CS-NON"));
        assertEquals("A320", turnaround.modelName());
        assertEquals(0.0, turnaround.averageHours());

        PaginatedResult<MaintenanceRecord> result = repository.search(null, null, null, null, 0, 10);
        assertNotNull(result);
    }
}
