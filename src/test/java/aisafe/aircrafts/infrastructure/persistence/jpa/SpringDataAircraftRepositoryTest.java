package aisafe.aircrafts.infrastructure.persistence.jpa;

import aisafe.aircrafts.domain.*;
import aisafe.shared.domain.PaginatedResult;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("jpa")
@Transactional
class SpringDataAircraftRepositoryTest {

    @Autowired
    private SpringDataAircraftRepository aircraftRepository;

    @Autowired
    private SpringDataAircraftModelRepository modelRepository;

    @Autowired
    private AircraftRepository domainAircraftRepository;

    @Autowired
    private AircraftModelRepository domainModelRepository;

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

    private AircraftModel createDomainModel(String name) {
        AircraftModel model = new AircraftModel(name, Manufacturer.AIRBUS, 20000.0, 6000.0, 800.0, null, 180);
        return domainModelRepository.save(model);
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

    @Test
    void ensureDomainRepositoryCount() {
        long initialCount = domainAircraftRepository.count();
        AircraftModel model = createDomainModel("A320-COUNT");
        Aircraft aircraft = new Aircraft(
                AircraftStatus.AVAILABLE,
                LocalDate.now(),
                model,
                new RegistrationNumber("CS-CNT"),
                150,
                5000.0,
                List.of()
        );
        domainAircraftRepository.save(aircraft);
        assertEquals(initialCount + 1, domainAircraftRepository.count());
    }

    @Test
    void ensureDomainRepositorySearchAircrafts() {
        AircraftModel model = createDomainModel("A320-SEARCH-DOM");
        Aircraft aircraft = new Aircraft(
                AircraftStatus.AVAILABLE,
                LocalDate.of(2021, 1, 1),
                model,
                new RegistrationNumber("CS-SCH"),
                150,
                5000.0,
                List.of("WiFi")
        );
        domainAircraftRepository.save(aircraft);

        PaginatedResult<Aircraft> res1 = domainAircraftRepository.searchAircrafts("A320-SEARCH-DOM", AircraftStatus.AVAILABLE, 2021, "WiFi", 0, 10);
        assertEquals(1, res1.totalElements());

        // Test with null status and other search filters
        PaginatedResult<Aircraft> res2 = domainAircraftRepository.searchAircrafts(null, null, null, null, 0, 10);
        assertTrue(res2.totalElements() > 0);
    }

    @Test
    void ensureDomainRepositoryFindAllAndPaginated() {
        AircraftModel model = createDomainModel("A320-FINDALL");
        Aircraft aircraft = new Aircraft(
                AircraftStatus.AVAILABLE,
                LocalDate.now(),
                model,
                new RegistrationNumber("CS-FAL"),
                150,
                5000.0,
                List.of()
        );
        domainAircraftRepository.save(aircraft);

        List<Aircraft> all = domainAircraftRepository.findAll();
        assertTrue(all.stream().anyMatch(a -> a.getRegistrationNumber().getNumber().equals("CS-FAL")));

        PaginatedResult<Aircraft> paginated = domainAircraftRepository.findAll(0, 10);
        assertTrue(paginated.data().stream().anyMatch(a -> a.getRegistrationNumber().getNumber().equals("CS-FAL")));
    }

    @Test
    void ensureDomainRepositoryExistsAndFindMethods() {
        AircraftModel model = createDomainModel("A320-EXISTS");
        Aircraft aircraft = new Aircraft(
                AircraftStatus.AVAILABLE,
                LocalDate.now(),
                model,
                new RegistrationNumber("CS-EXS"),
                150,
                5000.0,
                List.of()
        );
        domainAircraftRepository.save(aircraft);

        assertTrue(domainAircraftRepository.existsByRegistrationNumber(new RegistrationNumber("CS-EXS")));
        assertFalse(domainAircraftRepository.existsByRegistrationNumber(new RegistrationNumber("CS-NEX")));

        assertTrue(domainAircraftRepository.existsByModelName("A320-EXISTS"));
        assertFalse(domainAircraftRepository.existsByModelName("B737-NEX"));

        Optional<Aircraft> found = domainAircraftRepository.findByRegistrationNumber(new RegistrationNumber("CS-EXS"));
        assertTrue(found.isPresent());
        assertEquals("CS-EXS", found.get().getRegistrationNumber().getNumber());
    }

    @Test
    void ensureDomainRepositorySaveExistingUpdatesRecord() {
        AircraftModel model = createDomainModel("A320-UPDATE");
        Aircraft aircraft = new Aircraft(
                AircraftStatus.AVAILABLE,
                LocalDate.now(),
                model,
                new RegistrationNumber("CS-UPD"),
                150,
                5000.0,
                List.of()
        );
        domainAircraftRepository.save(aircraft);

        aircraft.changeStatus(AircraftStatus.INACTIVE);
        domainAircraftRepository.save(aircraft);

        Optional<Aircraft> found = domainAircraftRepository.findByRegistrationNumber(new RegistrationNumber("CS-UPD"));
        assertTrue(found.isPresent());
        assertEquals(AircraftStatus.INACTIVE, found.get().getStatus());
    }

    @Test
    void ensureDomainRepositorySaveThrowsWhenModelNotFound() {
        AircraftModel model = new AircraftModel("NON-EXISTENT-MODEL", Manufacturer.AIRBUS, 20000.0, 5000.0, 800.0, null, 150);
        Aircraft aircraft = new Aircraft(
                AircraftStatus.AVAILABLE,
                LocalDate.now(),
                model,
                new RegistrationNumber("CS-ERR"),
                150,
                5000.0,
                List.of()
        );

        assertThrows(AircraftModelNotFoundException.class, () -> {
            domainAircraftRepository.save(aircraft);
        });
    }

    @Test
    void ensureDomainRepositoryDelete() {
        AircraftModel model = createDomainModel("A320-DELETE");
        Aircraft aircraft = new Aircraft(
                AircraftStatus.AVAILABLE,
                LocalDate.now(),
                model,
                new RegistrationNumber("CS-DEL"),
                150,
                5000.0,
                List.of()
        );
        domainAircraftRepository.save(aircraft);
        assertTrue(domainAircraftRepository.existsByRegistrationNumber(new RegistrationNumber("CS-DEL")));

        domainAircraftRepository.delete(aircraft);
        assertFalse(domainAircraftRepository.existsByRegistrationNumber(new RegistrationNumber("CS-DEL")));
    }

    @Test
    void ensureDomainRepositoryDeleteThrowsWhenNotFound() {
        AircraftModel model = createDomainModel("A320-DEL-ERR");
        Aircraft aircraft = new Aircraft(
                AircraftStatus.AVAILABLE,
                LocalDate.now(),
                model,
                new RegistrationNumber("CS-DER"),
                150,
                5000.0,
                List.of()
        );

        assertThrows(AircraftNotFoundException.class, () -> {
            domainAircraftRepository.delete(aircraft);
        });
    }

    @Test
    void ensureDomainRepositoryFindVersionFor() {
        AircraftModel model = createDomainModel("A320-VERSION");
        Aircraft aircraft = new Aircraft(
                AircraftStatus.AVAILABLE,
                LocalDate.now(),
                model,
                new RegistrationNumber("CS-VER"),
                150,
                5000.0,
                List.of()
        );
        domainAircraftRepository.save(aircraft);

        Long version = domainAircraftRepository.findVersionFor(new RegistrationNumber("CS-VER"));
        assertNotNull(version);

        Long nonExistentVersion = domainAircraftRepository.findVersionFor(new RegistrationNumber("CS-NVX"));
        assertEquals(0L, nonExistentVersion);
    }
}
