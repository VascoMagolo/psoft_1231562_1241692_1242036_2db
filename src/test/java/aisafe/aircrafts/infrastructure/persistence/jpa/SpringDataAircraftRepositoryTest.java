package aisafe.aircrafts.infrastructure.persistence.jpa;

import aisafe.aircrafts.domain.AircraftStatus;
import aisafe.aircrafts.domain.Manufacturer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("jpa")
@Transactional
class SpringDataAircraftRepositoryTest {

    @Autowired
    private SpringDataAircraftRepository aircraftRepository;

    @Autowired
    private SpringDataAircraftModelRepository modelRepository;

    private AircraftModelJpaEntity createModel(String name) {
        AircraftModelJpaEntity model = new AircraftModelJpaEntity();
        model.setModelName(name);
        model.setManufacturer(Manufacturer.AIRBUS);
        model.setFuelCapacity(20000.0);
        model.setMaxRange(6000.0);
        model.setCruisingSpeed(800.0);
        model.setMaximumSeatingCapacity(180);
        return modelRepository.save(model);
    }

    @Test
    void ensureSearchAircraftsReturnsExpectedResults() {
        AircraftModelJpaEntity model = createModel("A320-SEARCH");

        AircraftJpaEntity aircraft = new AircraftJpaEntity();
        aircraft.setRegistrationNumber(new RegistrationNumberJpaEmbeddable("CS-TPA-S"));
        aircraft.setModel(model);
        aircraft.setStatus(AircraftStatus.AVAILABLE.name());
        aircraft.setManufacturingDate(LocalDate.of(2022, 5, 10));
        aircraft.setSeatCapacity(150);
        aircraft.setRange(5000.0);
        aircraft.setFeatures(List.of("WiFi"));
        aircraftRepository.save(aircraft);

        Page<AircraftJpaEntity> result = aircraftRepository.searchAircrafts(
                "A320-SEARCH", "AVAILABLE", 2022, "WiFi", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("CS-TPA-S", result.getContent().get(0).getRegistrationNumber().getNumber());
    }

    @Test
    void ensureExistsByRegistrationNumber() {
        AircraftModelJpaEntity model = createModel("B737-EX");

        AircraftJpaEntity aircraft = new AircraftJpaEntity();
        aircraft.setRegistrationNumber(new RegistrationNumberJpaEmbeddable("CS-TPB-E"));
        aircraft.setModel(model);
        aircraft.setStatus(AircraftStatus.AVAILABLE.name());
        aircraft.setManufacturingDate(LocalDate.now());
        aircraft.setSeatCapacity(100);
        aircraft.setRange(1000.0);
        aircraftRepository.save(aircraft);

        assertTrue(aircraftRepository.existsByRegistrationNumber(new RegistrationNumberJpaEmbeddable("CS-TPB-E")));
        assertFalse(aircraftRepository.existsByRegistrationNumber(new RegistrationNumberJpaEmbeddable("NON-EXISTENT")));
    }

    @Test
    void ensureUniqueRegistrationNumberConstraint() {
        AircraftModelJpaEntity model = createModel("MODEL-UNIQUE");

        AircraftJpaEntity a1 = new AircraftJpaEntity();
        a1.setRegistrationNumber(new RegistrationNumberJpaEmbeddable("CS-DUP"));
        a1.setModel(model);
        a1.setStatus(AircraftStatus.AVAILABLE.name());
        a1.setManufacturingDate(LocalDate.now());
        a1.setSeatCapacity(100);
        a1.setRange(1000.0);
        aircraftRepository.save(a1);

        AircraftJpaEntity a2 = new AircraftJpaEntity();
        a2.setRegistrationNumber(new RegistrationNumberJpaEmbeddable("CS-DUP")); // Duplicate
        a2.setModel(model);
        a2.setStatus(AircraftStatus.AVAILABLE.name());
        a2.setManufacturingDate(LocalDate.now());
        a2.setSeatCapacity(100);
        a2.setRange(1000.0);

        assertThrows(DataIntegrityViolationException.class, () -> {
            aircraftRepository.saveAndFlush(a2);
        });
    }
}
