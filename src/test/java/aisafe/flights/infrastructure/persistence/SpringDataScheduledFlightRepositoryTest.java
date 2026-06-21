package aisafe.flights.infrastructure.persistence;

import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.aircrafts.infrastructure.persistence.jpa.AircraftJpaEntity;
import aisafe.aircrafts.infrastructure.persistence.jpa.RegistrationNumberJpaEmbeddable;
import aisafe.aircrafts.infrastructure.persistence.jpa.SpringDataAircraftRepository;
import aisafe.aircrafts.infrastructure.persistence.jpa.AircraftModelJpaEntity;
import aisafe.aircrafts.infrastructure.persistence.jpa.SpringDataAircraftModelRepository;
import aisafe.flights.domain.FlightStatus;
import aisafe.flights.domain.ScheduledFlight;
import aisafe.flights.domain.ScheduledFlightRepository;
import aisafe.flights.infrastructure.persistence.jpa.*;
import aisafe.routes.domain.RouteStatus;
import aisafe.routes.infrastructure.persistence.jpa.RouteJpaEntity;
import aisafe.routes.infrastructure.persistence.jpa.SpringDataRouteRepository;
import aisafe.aircrafts.domain.Manufacturer;
import aisafe.airports.domain.IataCode;
import aisafe.shared.domain.PaginatedResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("jpa")
@Transactional
class SpringDataScheduledFlightRepositoryTest {

    @Autowired
    private SpringDataScheduledFlightRepository flightRepo;

    @Autowired
    private SpringDataAircraftRepository aircraftRepo;
    
    @Autowired
    private SpringDataAircraftModelRepository modelRepo;

    @Autowired
    private SpringDataRouteRepository routeRepo;

    @Autowired
    private ScheduledFlightRepository domainRepository;

    private RouteJpaEntity route1;
    private RouteJpaEntity route2;
    private AircraftJpaEntity aircraft;
    private OffsetDateTime now;

    @BeforeEach
    void setUp() {
        flightRepo.deleteAll();
        aircraftRepo.deleteAll();
        modelRepo.deleteAll();
        routeRepo.deleteAll();

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

        now = OffsetDateTime.now();

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
    void ensureDomainRepositoryCountAndFindAll() {
        assertEquals(5, domainRepository.count());
        assertEquals(5, domainRepository.findAll().size());
    }

    @Test
    void ensureDomainRepositoryFindById() {
        List<ScheduledFlight> all = domainRepository.findAll();
        assertFalse(all.isEmpty());
        Long id = all.get(0).getId();
        Optional<ScheduledFlight> found = domainRepository.findById(id);
        assertTrue(found.isPresent());
        assertEquals(id, found.get().getId());
    }

    @Test
    void ensureDomainRepositorySaveAndDelete() {
        ScheduledFlight flight = new ScheduledFlight(
                null,
                now.plusDays(10),
                now.plusDays(10).plusHours(2),
                FlightStatus.SCHEDULED,
                new IataCode("OPO"),
                new IataCode("LIS"),
                new RegistrationNumber("CS-TPA")
        );

        ScheduledFlight saved = domainRepository.save(flight);
        assertNotNull(saved.getId());
        assertEquals(6, domainRepository.count());

        domainRepository.delete(saved);
        assertEquals(5, domainRepository.count());
    }

    @Test
    void ensureDomainRepositoryQueries() {
        RegistrationNumber reg = new RegistrationNumber("CS-TPA");
        
        var flightsByReg = domainRepository.findByAircraftRegistration(reg);
        assertEquals(5, flightsByReg.size());

        var flightsForUtil = domainRepository.findFlightsForUtilization(reg, now.minusDays(6), now.minusDays(3));
        assertEquals(2, flightsForUtil.size());

        assertTrue(domainRepository.existsByAircraftRegistration(reg));
        assertFalse(domainRepository.existsByAircraftRegistration(new RegistrationNumber("CS-TPB")));

        assertTrue(domainRepository.existsByOverlappingSchedule(reg, now.minusDays(5).plusMinutes(10), now.minusDays(5).plusMinutes(50)));
        assertFalse(domainRepository.existsByOverlappingSchedule(reg, now.plusDays(20), now.plusDays(20).plusHours(1)));

        assertEquals(3, domainRepository.countByRoute(new IataCode("OPO"), new IataCode("LIS")));
        assertEquals(2, domainRepository.countByRoute(new IataCode("LIS"), new IataCode("MAD")));
    }

    @Test
    void ensureDomainRepositoryHoursCalculation() {
        RegistrationNumber reg = new RegistrationNumber("CS-TPA");
        Double totalHours = domainRepository.calculateTotalOperationalHoursByRegistration(reg);
        assertEquals(4.0, totalHours, 0.01);

        Double hoursSince = domainRepository.calculateOperationalHoursSince(reg, now.minusDays(3));
        assertEquals(2.0, hoursSince, 0.01);
    }

    @Test
    void ensureDomainRepositoryTopModelsAndReport() {
        var topHours = domainRepository.findTopModelsByFlightHours(5);
        assertFalse(topHours.isEmpty());
        assertEquals("B737", topHours.get(0).modelName());

        var topAssignments = domainRepository.findTopModelsByAssignments(5);
        assertFalse(topAssignments.isEmpty());
        assertEquals("B737", topAssignments.get(0).modelName());

        var report = domainRepository.getFlightUtilizationReport(now.minusDays(6), now, 0, 10);
        assertNotNull(report);
        assertEquals(2, report.totalElements());
    }

    @Test
    void ensureScheduledFlightMapperNullAndConstructor() {
        assertNull(ScheduledFlightMapper.toDomain(null));
        ScheduledFlightMapper mapper = new ScheduledFlightMapper();
        assertNotNull(mapper);
    }
}
