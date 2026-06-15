package aisafe.flights.infrastructure.persistence;

import aisafe.aircrafts.infrastructure.persistence.jpa.AircraftJpaEntity;
import aisafe.aircrafts.infrastructure.persistence.jpa.AircraftModelJpaEntity;
import aisafe.aircrafts.infrastructure.persistence.jpa.RegistrationNumberJpaEmbeddable;
import aisafe.flights.domain.FlightStatus;
import aisafe.routes.infrastructure.persistence.jpa.RouteJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("jpa")
class SpringDataScheduledFlightRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SpringDataScheduledFlightRepository repository;

    @Test
    void ensureHasOverlappingFlightsDetectsOverlap() {
        // Setup aircraft and route
        AircraftModelJpaEntity model = new AircraftModelJpaEntity("A320", "Airbus", 6000.0, 180, 850, "path/to/img");
        entityManager.persist(model);
        
        AircraftJpaEntity aircraft = new AircraftJpaEntity(new RegistrationNumberJpaEmbeddable("CS-TPA"), model, 5800.0, 180, OffsetDateTime.now());
        entityManager.persist(aircraft);
        
        RouteJpaEntity route = new RouteJpaEntity("OPO", "LIS", 45, 300.0, 150);
        entityManager.persist(route);
        
        OffsetDateTime departure = OffsetDateTime.now().plusDays(1);
        OffsetDateTime arrival = departure.plusHours(2);
        
        ScheduledFlightJpaEntity flight = new ScheduledFlightJpaEntity(departure, arrival, FlightStatus.SCHEDULED, route, aircraft);
        entityManager.persist(flight);
        entityManager.flush();

        // Exact overlap
        assertTrue(repository.hasOverlappingFlights("CS-TPA", departure, arrival));
        
        // Starts before, ends after
        assertTrue(repository.hasOverlappingFlights("CS-TPA", departure.minusHours(1), arrival.plusHours(1)));
        
        // Starts during
        assertTrue(repository.hasOverlappingFlights("CS-TPA", departure.plusMinutes(30), arrival.plusHours(1)));
        
        // No overlap
        assertFalse(repository.hasOverlappingFlights("CS-TPA", departure.minusHours(5), departure.minusHours(3)));
    }

    @Test
    void ensureCountByRouteReturnsCorrectValue() {
        AircraftModelJpaEntity model = new AircraftModelJpaEntity("A320", "Airbus", 6000.0, 180, 850, "path/to/img");
        entityManager.persist(model);
        
        AircraftJpaEntity aircraft = new AircraftJpaEntity(new RegistrationNumberJpaEmbeddable("CS-TPA"), model, 5800.0, 180, OffsetDateTime.now());
        entityManager.persist(aircraft);
        
        RouteJpaEntity route = new RouteJpaEntity("OPO", "LIS", 45, 300.0, 150);
        entityManager.persist(route);
        
        ScheduledFlightJpaEntity flight = new ScheduledFlightJpaEntity(OffsetDateTime.now(), OffsetDateTime.now().plusHours(1), FlightStatus.SCHEDULED, route, aircraft);
        entityManager.persist(flight);
        entityManager.flush();

        assertEquals(1, repository.countByRoute("OPO", "LIS"));
        assertEquals(0, repository.countByRoute("LIS", "OPO"));
    }
}
