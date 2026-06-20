package aisafe.flights.infrastructure.persistence;

import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.aircrafts.infrastructure.persistence.jpa.AircraftJpaEntity;
import aisafe.aircrafts.infrastructure.persistence.jpa.RegistrationNumberJpaEmbeddable;
import aisafe.aircrafts.infrastructure.persistence.jpa.SpringDataAircraftRepository;
import aisafe.aircrafts.infrastructure.persistence.jpa.AircraftModelJpaEntity;
import aisafe.aircrafts.infrastructure.persistence.jpa.SpringDataAircraftModelRepository;
import aisafe.flights.domain.FlightStatus;
import aisafe.flights.infrastructure.persistence.jpa.RouteUtilizationProjection;
import aisafe.flights.infrastructure.persistence.jpa.ScheduledFlightJpaEntity;
import aisafe.flights.infrastructure.persistence.jpa.ScheduledFlightJpaRepository;
import aisafe.flights.infrastructure.persistence.jpa.SpringDataScheduledFlightRepository;
import aisafe.routes.domain.RouteStatus;
import aisafe.routes.infrastructure.persistence.jpa.RouteJpaEntity;
import aisafe.routes.infrastructure.persistence.jpa.SpringDataRouteRepository;
import aisafe.aircrafts.domain.Manufacturer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import(ScheduledFlightJpaRepository.class)
class SpringDataScheduledFlightRepositoryTest {

    @Autowired
    private SpringDataScheduledFlightRepository flightRepo;

    @Autowired
    private SpringDataAircraftRepository aircraftRepo;
    
    @Autowired
    private SpringDataAircraftModelRepository modelRepo;

    @Autowired
    private SpringDataRouteRepository routeRepo;

    private RouteJpaEntity route1;
    private RouteJpaEntity route2;
    private AircraftJpaEntity aircraft;

    @BeforeEach
    void setUp() {
        AircraftModelJpaEntity model = new AircraftModelJpaEntity();
        model.setModelName("B737");
        model.setManufacturer(Manufacturer.BOEING);
        model.setMaximumSeatingCapacity(200);
        model.setMaxRange(20000.0);
        model.setFuelCapacity(5000.0);
        model.setCruisingSpeed(800.0);
        model = modelRepo.save(model);

        aircraft = new AircraftJpaEntity();
        aircraft.setRegistrationNumber(new RegistrationNumberJpaEmbeddable("CS-TPA"));
        aircraft.setModel(model);
        aircraft.setManufacturingDate(LocalDate.now());
        aircraft.setStatus("ACTIVE");
        aircraft.setSeatCapacity(200);
        aircraft.setRange(5000.0);
        aircraft = aircraftRepo.save(aircraft);

        route1 = new RouteJpaEntity("OPO", "LIS", 60, 300.0, 100, RouteStatus.ACTIVE);
        route1 = routeRepo.save(route1);
        
        route2 = new RouteJpaEntity("LIS", "MAD", 90, 500.0, 100, RouteStatus.ACTIVE);
        route2 = routeRepo.save(route2);

        OffsetDateTime now = OffsetDateTime.now();

        // 3 completed flights for route1
        flightRepo.save(new ScheduledFlightJpaEntity(now.minusDays(5), now.minusDays(5).plusHours(1), FlightStatus.COMPLETED, route1, aircraft));
        flightRepo.save(new ScheduledFlightJpaEntity(now.minusDays(4), now.minusDays(4).plusHours(1), FlightStatus.COMPLETED, route1, aircraft));
        flightRepo.save(new ScheduledFlightJpaEntity(now.minusDays(1), now.minusDays(1).plusHours(1), FlightStatus.COMPLETED, route1, aircraft));
        
        // 1 completed flight for route2
        flightRepo.save(new ScheduledFlightJpaEntity(now.minusDays(2), now.minusDays(2).plusHours(1), FlightStatus.COMPLETED, route2, aircraft));
        
        // 1 scheduled (not completed) flight for route2
        flightRepo.save(new ScheduledFlightJpaEntity(now.plusDays(1), now.plusDays(1).plusHours(1), FlightStatus.SCHEDULED, route2, aircraft));
    }

    @Test
    void ensureFindFlightUtilizationReportsAggregatesCorrectly() {
        List<RouteUtilizationProjection> reports = flightRepo.findFlightUtilizationReports(null, null, PageRequest.of(0, 10)).getContent();
        
        assertEquals(2, reports.size());
        
        // First should be route1 (OPO to LIS) with 3 flights
        assertEquals(route1.getId(), reports.get(0).getRouteId());
        assertEquals("OPO", reports.get(0).getOriginCode());
        assertEquals("LIS", reports.get(0).getDestinationCode());
        assertEquals(3L, reports.get(0).getFlightCount());
        
        // Second should be route2 (LIS to MAD) with 1 flight
        assertEquals(route2.getId(), reports.get(1).getRouteId());
        assertEquals("LIS", reports.get(1).getOriginCode());
        assertEquals("MAD", reports.get(1).getDestinationCode());
        assertEquals(1L, reports.get(1).getFlightCount());
    }

    @Test
    void ensureFindFlightUtilizationReportsFiltersByDate() {
        OffsetDateTime start = OffsetDateTime.now().minusDays(3); // Covers route1(1 flight) and route2(1 flight)
        OffsetDateTime end = OffsetDateTime.now();
        
        List<RouteUtilizationProjection> reports = flightRepo.findFlightUtilizationReports(start, end, PageRequest.of(0, 10)).getContent();
        
        assertEquals(2, reports.size());
        assertEquals(1L, reports.get(0).getFlightCount());
        assertEquals(1L, reports.get(1).getFlightCount());
    }

    @Test
    void ensureCalculateOperationalHoursSinceWorks() {
        OffsetDateTime since = OffsetDateTime.now().minusDays(3);
        Double hours = flightRepo.calculateOperationalHoursSince("CS-TPA", since);
        assertEquals(2.0, hours, 0.001);
    }
}
