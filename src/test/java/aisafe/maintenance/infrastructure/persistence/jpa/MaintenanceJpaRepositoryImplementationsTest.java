package aisafe.maintenance.infrastructure.persistence.jpa;

import aisafe.aircrafts.domain.ModelName;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.maintenance.domain.*;
import aisafe.shared.domain.PaginatedResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("jpa")
@Transactional
class MaintenanceJpaRepositoryImplementationsTest {

    @Autowired
    private MaintenancePartRepository partRepository;

    @Autowired
    private MaintenanceTemplateRepository templateRepository;

    @Autowired
    private MaintenanceRecordRepository recordRepository;

    @Test
    void ensurePartJpaRepositorySaveNewAndExisting() {
        MaintenancePart part = new MaintenancePart(
                "PN-REP-1", "Part 1", "Desc 1", 10, 2, MaintenanceComponent.ENGINE
        );
        MaintenancePart saved = partRepository.save(part);
        assertNotNull(saved);
        assertEquals("PN-REP-1", saved.getPartNumber());

        long count = partRepository.count();
        assertTrue(count > 0);

        List<MaintenancePart> all = partRepository.findAll();
        assertTrue(all.stream().anyMatch(p -> p.getPartNumber().equals("PN-REP-1")));

        Optional<MaintenancePart> found = partRepository.findByPartNumber("PN-REP-1");
        assertTrue(found.isPresent());
        assertEquals("Part 1", found.get().getName());

        MaintenancePart updatedPart = new MaintenancePart(
                "PN-REP-1", "Part 1 Updated", "Desc 1 Updated", 15, 3, MaintenanceComponent.ENGINE
        );
        partRepository.save(updatedPart);

        Optional<MaintenancePart> foundUpdated = partRepository.findByPartNumber("PN-REP-1");
        assertTrue(foundUpdated.isPresent());
        assertEquals("Part 1 Updated", foundUpdated.get().getName());
        assertEquals(15, foundUpdated.get().getStockQuantity());
    }

    @Test
    void ensurePartJpaRepositoryExistsAndSearchAndDelete() {
        MaintenancePart part = new MaintenancePart(
                "PN-REP-2", "Part 2", "Desc 2", 10, 2, MaintenanceComponent.AIRFRAME
        );
        partRepository.save(part);

        assertTrue(partRepository.existsByPartNumber("PN-REP-2"));
        assertFalse(partRepository.existsByPartNumber("PN-REP-NON"));

        PaginatedResult<MaintenancePart> searchResult = partRepository.searchParts(
                "PN-REP-2", "Part", MaintenanceComponent.AIRFRAME, false, 0, 10
        );
        assertEquals(1, searchResult.totalElements());
        assertEquals("PN-REP-2", searchResult.data().get(0).getPartNumber());

        PaginatedResult<MaintenancePart> searchLowStock = partRepository.searchParts(
                null, null, null, true, 0, 10
        );
        assertNotNull(searchLowStock);

        PaginatedResult<MaintenancePart> searchNullLowStock = partRepository.searchParts(
                null, null, null, null, 0, 10
        );
        assertNotNull(searchNullLowStock);

        partRepository.delete(part);
        assertFalse(partRepository.existsByPartNumber("PN-REP-2"));

        assertThrows(MaintenancePartNotFoundException.class, () -> {
            partRepository.delete(part);
        });
    }

    @Test
    void ensureTemplateJpaRepositorySaveNewAndExisting() {
        MaintenanceTemplate template = new MaintenanceTemplate(
                "Temp 1", MaintenanceType.INSPECTION, new java.util.ArrayList<>(List.of(new ModelName("A320"))), new java.util.ArrayList<>(List.of("Task 1")), 500, 30
        );
        templateRepository.save(template);

        long count = templateRepository.count();
        assertTrue(count > 0);

        List<MaintenanceTemplate> all = templateRepository.findAll();
        assertTrue(all.stream().anyMatch(t -> t.getName().equals("Temp 1")));

        Optional<MaintenanceTemplate> found = templateRepository.findByName("Temp 1");
        assertTrue(found.isPresent());
        assertEquals("Temp 1", found.get().getName());

        MaintenanceTemplate updatedTemplate = new MaintenanceTemplate(
                "Temp 1", MaintenanceType.OVERHAUL, new java.util.ArrayList<>(List.of(new ModelName("B737"))), new java.util.ArrayList<>(List.of("Task 1", "Task 2")), 1000, 60
        );
        templateRepository.save(updatedTemplate);

        Optional<MaintenanceTemplate> foundUpdated = templateRepository.findByName("Temp 1");
        assertTrue(foundUpdated.isPresent());
        assertEquals(MaintenanceType.OVERHAUL, foundUpdated.get().getTemplateType());
        assertEquals(1000, foundUpdated.get().getIntervalFlightHours());
    }

    @Test
    void ensureTemplateJpaRepositoryExistsAndDelete() {
        MaintenanceTemplate template = new MaintenanceTemplate(
                "Temp 2", MaintenanceType.INSPECTION, new java.util.ArrayList<>(List.of(new ModelName("A320"))), new java.util.ArrayList<>(List.of("Task 2")), 500, 30
        );
        templateRepository.save(template);

        assertTrue(templateRepository.existsByName("Temp 2"));
        assertFalse(templateRepository.existsByName("Temp NON"));

        templateRepository.delete(template);
        assertFalse(templateRepository.existsByName("Temp 2"));

        assertThrows(MaintenanceTemplateNotFoundException.class, () -> {
            templateRepository.delete(template);
        });
    }

    @Test
    void ensureRecordJpaRepositorySaveNewAndExistingAndFind() {
        MaintenancePart part = new MaintenancePart(
                "PN-REC-1", "Part Rec 1", "Desc", 10, 2, MaintenanceComponent.ENGINE
        );
        partRepository.save(part);

        MaintenanceTemplate template = new MaintenanceTemplate(
                "Temp Rec 1", MaintenanceType.INSPECTION, new java.util.ArrayList<>(List.of(new ModelName("A320"))), new java.util.ArrayList<>(List.of("Task")), 500, 30
        );
        templateRepository.save(template);

        UUID recordId = UUID.randomUUID();
        MaintenanceRecord record = new MaintenanceRecord(
                recordId, "Record 1", LocalDateTime.of(2026, 6, 20, 12, 0), 5,
                new java.util.ArrayList<>(List.of(part)), "Notes", template, MaintenanceStatus.PLANNED,
                Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA"),
                BigDecimal.valueOf(150.0), null
        );

        MaintenanceRecord saved = recordRepository.save(record);
        assertNotNull(saved);
        assertEquals(recordId, saved.getRecordId());

        long count = recordRepository.count();
        assertTrue(count > 0);

        Optional<MaintenanceRecord> found = recordRepository.findByRecordId(recordId);
        assertTrue(found.isPresent());
        assertEquals("Record 1", found.get().getDescription());

        Long version = recordRepository.findVersionFor(recordId);
        assertNotNull(version);

        MaintenanceRecord updatedRecord = new MaintenanceRecord(
                recordId, "Record 1", LocalDateTime.of(2026, 6, 20, 12, 0), 5,
                new java.util.ArrayList<>(List.of(part)), "Updated Notes", template, MaintenanceStatus.IN_PROGRESS,
                Set.of(MaintenanceComponent.ENGINE), new RegistrationNumber("CS-TPA"),
                BigDecimal.valueOf(150.0), null
        );
        recordRepository.save(updatedRecord);

        Optional<MaintenanceRecord> foundUpdated = recordRepository.findByRecordId(recordId);
        assertTrue(foundUpdated.isPresent());
        assertEquals("Updated Notes", foundUpdated.get().getNotes());
        assertEquals(MaintenanceStatus.IN_PROGRESS, foundUpdated.get().getStatus());
    }

    @Test
    void ensureRecordJpaRepositorySearchQueriesAndCost() {
        MaintenancePart part = new MaintenancePart(
                "PN-REC-2", "Part Rec 2", "Desc", 10, 2, MaintenanceComponent.AVIONICS
        );
        partRepository.save(part);

        MaintenanceTemplate template = new MaintenanceTemplate(
                "Temp Rec 2", MaintenanceType.INSPECTION, new java.util.ArrayList<>(List.of(new ModelName("A320"))), new java.util.ArrayList<>(List.of("Task")), 500, 30
        );
        templateRepository.save(template);

        UUID recordId = UUID.randomUUID();
        MaintenanceRecord record = new MaintenanceRecord(
                recordId, "Record 2", LocalDateTime.of(2026, 6, 20, 12, 0), 5,
                new java.util.ArrayList<>(List.of(part)), "Notes", template, MaintenanceStatus.COMPLETED,
                Set.of(MaintenanceComponent.AVIONICS), new RegistrationNumber("CS-TPB"),
                BigDecimal.valueOf(250.00), LocalDateTime.of(2026, 6, 21, 12, 0)
        );
        recordRepository.save(record);

        assertTrue(recordRepository.existsByStartDateAndTemplate(LocalDateTime.of(2026, 6, 20, 12, 0), template));
        assertFalse(recordRepository.existsByStartDateAndTemplate(LocalDateTime.of(2026, 6, 20, 12, 0), new MaintenanceTemplate("NON-EXISTENT-TEMP", MaintenanceType.INSPECTION, new java.util.ArrayList<>(List.of(new ModelName("A320"))), new java.util.ArrayList<>(List.of("Verify")), 100, 30)));

        assertTrue(recordRepository.existsByPartsContaining(part));
        assertFalse(recordRepository.existsByPartsContaining(new MaintenancePart("NON-EXISTENT-PART", "Non", "Desc", 10, 1, MaintenanceComponent.ENGINE)));

        assertTrue(recordRepository.existsByTemplate(template));

        assertTrue(recordRepository.existsByAircraftRegistration(new RegistrationNumber("CS-TPB")));

        List<MaintenanceRecord> completed = recordRepository.findCompletedByAircraft(new RegistrationNumber("CS-TPB"));
        assertEquals(1, completed.size());

        PaginatedResult<MaintenanceRecord> byAircraft = recordRepository.findByAircraftRegistration(new RegistrationNumber("CS-TPB"), 0, 10);
        assertEquals(1, byAircraft.totalElements());

        List<MaintenanceRecord> all = recordRepository.findAll();
        assertTrue(all.size() > 0);

        PaginatedResult<MaintenanceRecord> byStatus = recordRepository.findByStatus(MaintenanceStatus.COMPLETED, 0, 10);
        assertTrue(byStatus.totalElements() > 0);

        PaginatedResult<MaintenanceRecord> searchRes = recordRepository.search(
                new RegistrationNumber("CS-TPB"), LocalDateTime.of(2026, 6, 19, 0, 0),
                LocalDateTime.of(2026, 6, 21, 23, 59), MaintenanceComponent.AVIONICS, 0, 10
        );
        assertEquals(1, searchRes.totalElements());

        Long hours = recordRepository.sumTotalMaintenanceHours();
        assertTrue(hours >= 5);

        BigDecimal cost = recordRepository.sumCostByAircraftRegistration(new RegistrationNumber("CS-TPB"));
        assertEquals(0, BigDecimal.valueOf(250.00).compareTo(cost));

        BigDecimal costList = recordRepository.sumCostByRegistrations(List.of("CS-TPB"));
        assertEquals(0, BigDecimal.valueOf(250.00).compareTo(costList));

        MaintenanceTurnaroundData turnaround = recordRepository.findAverageTurnaroundByRegistrations("A320", List.of("CS-TPB"));
        assertEquals("A320", turnaround.modelName());
        assertEquals(24.0, turnaround.averageHours());

        recordRepository.delete(record);
        assertFalse(recordRepository.existsByAircraftRegistration(new RegistrationNumber("CS-TPB")));
    }

    @Test
    void ensureMaintenanceRecordMapperRoundtrip() {
        assertNull(MaintenanceRecordMapper.toDomain(null));

        MaintenancePartJpaEntity partJpa = new MaintenancePartJpaEntity("P-MAP", "Part Map", "Desc", 10, 2, MaintenanceComponent.ENGINE);
        MaintenanceTemplateJpaEntity templateJpa = new MaintenanceTemplateJpaEntity("Temp Map", MaintenanceType.INSPECTION, List.of("A320"), List.of("Task"), 500, 30);

        UUID recordId = UUID.randomUUID();
        MaintenanceRecordJpaEntity entity = new MaintenanceRecordJpaEntity(
                recordId, "Description", LocalDateTime.of(2026, 6, 20, 12, 0), 10, "Notes",
                List.of(partJpa), templateJpa, MaintenanceStatus.PLANNED,
                java.util.Collections.singleton(MaintenanceComponent.ENGINE),
                new RegistrationNumberJpaEmbeddable("CS-TPA"), BigDecimal.valueOf(500.0)
        );
        entity.setCompletedAt(LocalDateTime.of(2026, 6, 20, 22, 0));

        MaintenanceRecord domain = MaintenanceRecordMapper.toDomain(entity);
        assertNotNull(domain);
        assertEquals(recordId, domain.getRecordId());
        assertEquals("Description", domain.getDescription());
        assertEquals("Notes", domain.getNotes());
        assertEquals(1, domain.getParts().size());
        assertEquals("P-MAP", domain.getParts().get(0).getPartNumber());
        assertEquals("Temp Map", domain.getTemplate().getName());
        assertEquals(MaintenanceStatus.PLANNED, domain.getStatus());
        assertEquals(Set.of(MaintenanceComponent.ENGINE), domain.getComponents());
        assertEquals("CS-TPA", domain.getAircraftRegistration().getNumber());
        assertEquals(0, BigDecimal.valueOf(500.0).compareTo(domain.getCost()));
        assertEquals(LocalDateTime.of(2026, 6, 20, 22, 0), domain.getCompletedAt());
    }
}
