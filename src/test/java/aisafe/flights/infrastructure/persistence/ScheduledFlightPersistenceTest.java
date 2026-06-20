package aisafe.flights.infrastructure.persistence;

import aisafe.aircrafts.domain.Manufacturer;
import aisafe.aircrafts.infrastructure.persistence.jpa.AircraftJpaEntity;
import aisafe.aircrafts.infrastructure.persistence.jpa.AircraftModelJpaEntity;
import aisafe.aircrafts.infrastructure.persistence.jpa.RegistrationNumberJpaEmbeddable;
import aisafe.flights.domain.FlightStatus;
import aisafe.flights.infrastructure.persistence.jpa.ModelUtilizationProjection;
import aisafe.flights.infrastructure.persistence.jpa.ScheduledFlightJpaEntity;
import aisafe.flights.infrastructure.persistence.jpa.SpringDataScheduledFlightRepository;
import aisafe.routes.infrastructure.persistence.jpa.RouteJpaEntity;
import aisafe.routes.domain.RouteStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("jpa")
class ScheduledFlightPersistenceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SpringDataScheduledFlightRepository repository;

    @Test
    void ensureFindTopModelsByFlightHoursWorks() {
        AircraftModelJpaEntity model = new AircraftModelJpaEntity();
        model.setModelName("B737");
        model.setManufacturer(Manufacturer.BOEING);
        model.setFuelCapacity(5000.0);
        model.setMaxRange(6000.0);
        model.setCruisingSpeed(800.0);
        model.setMaximumSeatingCapacity(180);
        entityManager.persist(model);

        AircraftJpaEntity aircraft = new AircraftJpaEntity();
        aircraft.setRegistrationNumber(new RegistrationNumberJpaEmbeddable("CS-TTA"));
        aircraft.setModel(model);
        aircraft.setManufacturingDate(LocalDate.now());
        aircraft.setStatus("ACTIVE");
        aircraft.setSeatCapacity(180);
        aircraft.setRange(6000.0);
        entityManager.persist(aircraft);

        RouteJpaEntity route = new RouteJpaEntity("LIS", "OPO", 45, 300.0, 150, RouteStatus.ACTIVE);
        entityManager.persist(route);

        OffsetDateTime departure = OffsetDateTime.now();
        OffsetDateTime arrival = departure.plusHours(2);
        
        ScheduledFlightJpaEntity flight = new ScheduledFlightJpaEntity(departure, arrival, FlightStatus.COMPLETED, route, aircraft);
        entityManager.persist(flight);
        
        entityManager.flush();

        List<ModelUtilizationProjection> result = repository.findTopModelsByFlightHours(PageRequest.of(0, 5));

        assertFalse(result.isEmpty());
        assertEquals("B737", result.get(0).getModelName());
        assertEquals(2L, result.get(0).getUtilizationValue());
    }

    @Test
    void ensureCalculateTotalOperationalHoursByRegistrationWorks() {
        AircraftModelJpaEntity model = new AircraftModelJpaEntity();
        model.setModelName("A320");
        model.setManufacturer(Manufacturer.AIRBUS);
        model.setFuelCapacity(6000.0);
        model.setMaxRange(7000.0);
        model.setCruisingSpeed(850.0);
        model.setMaximumSeatingCapacity(150);
        entityManager.persist(model);

        AircraftJpaEntity aircraft = new AircraftJpaEntity();
        aircraft.setRegistrationNumber(new RegistrationNumberJpaEmbeddable("CS-TTB"));
        aircraft.setModel(model);
        aircraft.setManufacturingDate(LocalDate.now());
        aircraft.setStatus("ACTIVE");
        aircraft.setSeatCapacity(150);
        aircraft.setRange(7000.0);
        entityManager.persist(aircraft);

        RouteJpaEntity route = new RouteJpaEntity("LIS", "MAD", 90, 800.0, 200, RouteStatus.ACTIVE);
        entityManager.persist(route);

        OffsetDateTime departure1 = OffsetDateTime.now().minusDays(1);
        OffsetDateTime arrival1 = departure1.plusHours(1).plusMinutes(30);
        
        ScheduledFlightJpaEntity flight1 = new ScheduledFlightJpaEntity(departure1, arrival1, FlightStatus.COMPLETED, route, aircraft);
        entityManager.persist(flight1);

        OffsetDateTime departure2 = OffsetDateTime.now().minusHours(5);
        OffsetDateTime arrival2 = departure2.plusHours(2).plusMinutes(15);
        
        ScheduledFlightJpaEntity flight2 = new ScheduledFlightJpaEntity(departure2, arrival2, FlightStatus.COMPLETED, route, aircraft);
        entityManager.persist(flight2);
        
        entityManager.flush();

        Double totalHours = repository.calculateTotalOperationalHoursByRegistration("CS-TTB");

        assertNotNull(totalHours);
        // (1.5 + 2.25) = 3.75
        assertEquals(3.75, totalHours, 0.01);
    }
}
