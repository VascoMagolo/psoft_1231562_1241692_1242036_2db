package aisafe.aircrafts.infrastructure.persistence.jpa;

import aisafe.aircrafts.domain.Manufacturer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("jpa")
@Transactional
class SpringDataAircraftModelRepositoryTest {

    @Autowired
    private SpringDataAircraftModelRepository repository;

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
}
