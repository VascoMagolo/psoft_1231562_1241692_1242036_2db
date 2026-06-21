package aisafe.aircrafts.infrastructure.persistence.jpa;

import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelRepository;
import aisafe.aircrafts.domain.Manufacturer;
import aisafe.shared.domain.PaginatedResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("jpa")
@Transactional
class SpringDataAircraftModelRepositoryTest {

    @Autowired
    private SpringDataAircraftModelRepository repository;

    @Autowired
    private AircraftModelRepository domainRepository;

    @Test
    void ensureSaveAndRetrieveModel() {
        AircraftModelJpaEntity entity = new AircraftModelJpaEntity();
        entity.setModelName("A320-TEST");
        entity.setManufacturer(Manufacturer.AIRBUS);
        entity.setFuelCapacity(26730.0);
        entity.setMaxRange(6150.0);
        entity.setCruisingSpeed(833.0);
        entity.setMaximumSeatingCapacity(180);

        repository.save(entity);

        Optional<AircraftModelJpaEntity> found = repository.findByModelName("A320-TEST");
        assertTrue(found.isPresent());
        assertEquals(Manufacturer.AIRBUS, found.get().getManufacturer());
    }

    @Test
    void ensureExistsByModelName() {
        AircraftModelJpaEntity entity = new AircraftModelJpaEntity();
        entity.setModelName("B737-TEST");
        entity.setManufacturer(Manufacturer.BOEING);
        entity.setFuelCapacity(25000.0);
        entity.setMaxRange(5000.0);
        entity.setCruisingSpeed(800.0);
        entity.setMaximumSeatingCapacity(150);
        repository.save(entity);

        assertTrue(repository.existsByModelName("B737-TEST"));
        assertFalse(repository.existsByModelName("NON_EXISTENT"));
    }

    @Test
    void ensureUniqueModelNameConstraint() {
        AircraftModelJpaEntity entity1 = new AircraftModelJpaEntity();
        entity1.setModelName("UNIQUE-MODEL");
        entity1.setManufacturer(Manufacturer.AIRBUS);
        entity1.setFuelCapacity(1000.0);
        entity1.setMaxRange(1000.0);
        entity1.setCruisingSpeed(500.0);
        entity1.setMaximumSeatingCapacity(100);
        repository.save(entity1);

        AircraftModelJpaEntity entity2 = new AircraftModelJpaEntity();
        entity2.setModelName("UNIQUE-MODEL"); // Duplicate
        entity2.setManufacturer(Manufacturer.BOEING);
        entity2.setFuelCapacity(2000.0);
        entity2.setMaxRange(2000.0);
        entity2.setCruisingSpeed(600.0);
        entity2.setMaximumSeatingCapacity(200);

        assertThrows(DataIntegrityViolationException.class, () -> {
            repository.saveAndFlush(entity2);
        });
    }

    @Test
    void ensureDomainRepositoryCount() {
        long initialCount = domainRepository.count();
        AircraftModel model = new AircraftModel("M1", Manufacturer.BOEING, 20000.0, 5000.0, 800.0, null, 150);
        domainRepository.save(model);
        assertEquals(initialCount + 1, domainRepository.count());
    }

    @Test
    void ensureDomainRepositorySaveAndRetrieve() {
        AircraftModel model = new AircraftModel("M2", Manufacturer.AIRBUS, 25000.0, 6000.0, 850.0, null, 180);
        domainRepository.save(model);

        Optional<AircraftModel> found = domainRepository.findByModelName("M2");
        assertTrue(found.isPresent());
        assertEquals(Manufacturer.AIRBUS, found.get().getManufacturer());
        assertTrue(domainRepository.existsByModelName("M2"));
    }

    @Test
    void ensureDomainRepositoryFindAll() {
        AircraftModel model = new AircraftModel("M3", Manufacturer.AIRBUS, 25000.0, 6000.0, 850.0, null, 180);
        domainRepository.save(model);

        List<AircraftModel> all = domainRepository.findAll();
        assertTrue(all.stream().anyMatch(m -> m.getModelName().equals("M3")));
    }

    @Test
    void ensureDomainRepositoryFindAllPaginated() {
        AircraftModel model = new AircraftModel("M4", Manufacturer.AIRBUS, 25000.0, 6000.0, 850.0, null, 180);
        domainRepository.save(model);

        PaginatedResult<AircraftModel> page = domainRepository.findAll(0, 10);
        assertNotNull(page);
        assertTrue(page.totalElements() > 0);
        assertTrue(page.data().stream().anyMatch(m -> m.getModelName().equals("M4")));
    }

    @Test
    void ensureDomainRepositoryDelete() {
        AircraftModel model = new AircraftModel("M5", Manufacturer.AIRBUS, 25000.0, 6000.0, 850.0, null, 180);
        domainRepository.save(model);
        assertTrue(domainRepository.existsByModelName("M5"));

        domainRepository.delete(model);
        assertFalse(domainRepository.existsByModelName("M5"));
    }

    @Test
    void ensureDomainRepositoryDeleteThrowsWhenNotFound() {
        AircraftModel model = new AircraftModel("M6-NON-EXISTENT", Manufacturer.AIRBUS, 25000.0, 6000.0, 850.0, null, 180);
        assertThrows(org.springframework.dao.InvalidDataAccessApiUsageException.class, () -> {
            domainRepository.delete(model);
        });
    }

    @Test
    void ensureDomainRepositorySaveExistingUpdatesRecord() {
        AircraftModel model = new AircraftModel("M7", Manufacturer.AIRBUS, 25000.0, 6000.0, 850.0, null, 180);
        domainRepository.save(model);

        AircraftModel updatedModel = new AircraftModel("M7", Manufacturer.AIRBUS, 30000.0, 7000.0, 900.0, null, 200);
        domainRepository.save(updatedModel);

        Optional<AircraftModel> found = domainRepository.findByModelName("M7");
        assertTrue(found.isPresent());
        assertEquals(30000.0, found.get().getFuelCapacity());
        assertEquals(7000.0, found.get().getMaxRange());
    }
}

