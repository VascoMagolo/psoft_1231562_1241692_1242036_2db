package aisafe.aircrafts.infrastructure.persistence.jpa;

import aisafe.aircrafts.domain.AircraftStatus;
import aisafe.aircrafts.domain.Manufacturer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

    @Test
    void ensureSearchAircraftsReturnsExpectedResults() {
        AircraftModelJpaEntity model = new AircraftModelJpaEntity();
        model.setModelName("A320-SEARCH");
        model.setManufacturer(Manufacturer.AIRBUS);
        model.setFuelCapacity(20000.0);
        model.setMaxRange(6000.0);
        model.setCruisingSpeed(800.0);
        model.setMaximumSeatingCapacity(180);
        modelRepository.save(model);

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
        AircraftModelJpaEntity model = new AircraftModelJpaEntity();
        model.setModelName("B737-EX");
        model.setManufacturer(Manufacturer.BOEING);
        model.setFuelCapacity(20000.0);
        model.setMaxRange(6000.0);
        model.setCruisingSpeed(800.0);
        model.setMaximumSeatingCapacity(180);
        modelRepository.save(model);

        AircraftJpaEntity aircraft = new AircraftJpaEntity();
        aircraft.setRegistrationNumber(new RegistrationNumberJpaEmbeddable("CS-TPB-E"));
        aircraft.setModel(model);
        aircraft.setStatus(AircraftStatus.AVAILABLE.name());
        aircraft.setManufacturingDate(LocalDate.now());
        aircraft.setSeatCapacity(150);
        aircraft.setRange(5000.0);
        aircraftRepository.save(aircraft);

        assertTrue(aircraftRepository.existsByRegistrationNumber(new RegistrationNumberJpaEmbeddable("CS-TPB-E")));
        assertFalse(aircraftRepository.existsByRegistrationNumber(new RegistrationNumberJpaEmbeddable("NON-EXISTENT")));
    }
}
