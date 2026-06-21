package aisafe.routes.infrastructure.persistence.jpa;

import aisafe.airports.domain.IataCode;
import aisafe.flights.infrastructure.persistence.jpa.SpringDataScheduledFlightRepository;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import aisafe.routes.domain.RouteStatus;
import aisafe.routes.domain.RouteSummaryData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("jpa")
@Transactional
class RouteRepositoryTest {

    @Autowired
    private SpringDataRouteRepository springRepo;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private SpringDataScheduledFlightRepository flightRepo;

    @Test
    void ensureFindByOriginAndDestinationWorksWithIataCode() {
        flightRepo.deleteAll();
        springRepo.deleteAll();
        IataCode origin = new IataCode("LIS");
        IataCode destination = new IataCode("OPO");
        Route route = new Route(origin.getCode(), destination.getCode(), 45, 300.0, 150);
        routeRepository.save(route);

        Optional<Route> found = routeRepository.findByOriginAndDestination(origin, destination);

        assertTrue(found.isPresent());
        assertEquals("LIS", found.get().getOrigin().getCode());
        assertEquals("OPO", found.get().getDestination().getCode());
    }

    @Test
    void ensureFindByOriginAndDestinationReturnsEmptyWhenNotFound() {
        flightRepo.deleteAll();
        springRepo.deleteAll();
        IataCode origin = new IataCode("LIS");
        IataCode destination = new IataCode("MAD");

        Optional<Route> found = routeRepository.findByOriginAndDestination(origin, destination);

        assertFalse(found.isPresent());
    }

    @Test
    void findCompatibleRoutesFiltersCorrectly() {
        flightRepo.deleteAll();
        springRepo.deleteAll();
        // Active, compatible
        springRepo.save(new RouteJpaEntity("XXX", "YYY", 45, 300.0, 100, RouteStatus.ACTIVE));
        // Active, not compatible (range)
        springRepo.save(new RouteJpaEntity("XXX", "ZZZ", 480, 7000.0, 100, RouteStatus.ACTIVE));
        // Active, not compatible (capacity)
        springRepo.save(new RouteJpaEntity("AAA", "BBB", 60, 500.0, 200, RouteStatus.ACTIVE));
        // Inactive, compatible
        springRepo.save(new RouteJpaEntity("CCC", "DDD", 35, 250.0, 50, RouteStatus.INACTIVE));

        List<RouteJpaEntity> result = springRepo.findCompatibleRoutes(6000.0, 150);

        assertEquals(1, result.size());
        assertEquals("XXX", result.get(0).getOriginCode().getCode());
        assertEquals("YYY", result.get(0).getDestinationCode().getCode());
    }

    @Test
    void ensureListSummariesForAirportReturnsRoutesWhereAirportIsOrigin() {
        flightRepo.deleteAll();
        springRepo.deleteAll();
        springRepo.save(new RouteJpaEntity("LIS", "OPO", 45, 300.0, 150, RouteStatus.ACTIVE));
        springRepo.save(new RouteJpaEntity("MAD", "CDG", 90, 800.0, 200, RouteStatus.ACTIVE));

        List<SpringDataRouteRepository.RouteSummaryRow> result = springRepo.findSummariesByAirportCode("LIS");

        assertEquals(1, result.size());
        assertEquals("LIS", result.get(0).getOriginCode());
        assertEquals("OPO", result.get(0).getDestinationCode());
    }

    @Test
    void ensureListSummariesForAirportReturnsRoutesWhereAirportIsDestination() {
        flightRepo.deleteAll();
        springRepo.deleteAll();
        springRepo.save(new RouteJpaEntity("CDG", "LIS", 120, 1200.0, 180, RouteStatus.ACTIVE));
        springRepo.save(new RouteJpaEntity("MAD", "CDG", 90, 800.0, 200, RouteStatus.ACTIVE));

        List<SpringDataRouteRepository.RouteSummaryRow> result = springRepo.findSummariesByAirportCode("LIS");

        assertEquals(1, result.size());
        assertEquals("CDG", result.get(0).getOriginCode());
        assertEquals("LIS", result.get(0).getDestinationCode());
    }

    @Test
    void ensureListSummariesForAirportExcludesRoutesForOtherAirports() {
        flightRepo.deleteAll();
        springRepo.deleteAll();
        springRepo.save(new RouteJpaEntity("MAD", "CDG", 90, 800.0, 200, RouteStatus.ACTIVE));
        springRepo.save(new RouteJpaEntity("FRA", "AMS", 60, 600.0, 120, RouteStatus.ACTIVE));

        List<SpringDataRouteRepository.RouteSummaryRow> result = springRepo.findSummariesByAirportCode("LIS");

        assertTrue(result.isEmpty());
    }

    @Test
    void ensureListSummariesForAirportReturnsVersionFromJpaEntity() {
        flightRepo.deleteAll();
        springRepo.deleteAll();
        springRepo.saveAndFlush(new RouteJpaEntity("LIS", "OPO", 45, 300.0, 150, RouteStatus.ACTIVE));

        List<SpringDataRouteRepository.RouteSummaryRow> result = springRepo.findSummariesByAirportCode("LIS");

        assertEquals(1, result.size());
        assertNotNull(result.get(0).getVersion());
    }

    @Test
    void ensureRouteRepositoryOperationsWork() {
        flightRepo.deleteAll();
        springRepo.deleteAll();

        // 1. Save new
        Route route = new Route("LIS", "OPO", 45, 300.0, 150);
        routeRepository.save(route);
        assertEquals(1, routeRepository.count());

        // 2. Save existing (updating)
        route.updateRoute(50, 350.0, 160);
        routeRepository.save(route);
        assertEquals(1, routeRepository.count());
        Route saved = routeRepository.findByOriginAndDestination(new IataCode("LIS"), new IataCode("OPO")).get();
        assertEquals(50, saved.getEstimatedFlightTime());

        // 3. Find Version
        Long version = routeRepository.findVersionFor(new IataCode("LIS"), new IataCode("OPO"));
        assertNotNull(version);

        // 4. Exists
        assertTrue(routeRepository.existsByOriginAndDestination(new IataCode("LIS"), new IataCode("OPO")));
        assertFalse(routeRepository.existsByOriginAndDestination(new IataCode("LIS"), new IataCode("MAD")));

        // 5. Find All Active
        List<Route> actives = routeRepository.findAllActive();
        assertEquals(1, actives.size());

        // 6. Find All
        List<Route> all = routeRepository.findAll();
        assertEquals(1, all.size());

        // 7. Find All Paginated
        var paginated = routeRepository.findAll(0, 10);
        assertEquals(1, paginated.data().size());
        assertEquals(1L, paginated.totalElements());

        // 8. Find By Origin Paginated
        var byOrigin = routeRepository.findByOrigin(new IataCode("LIS"), 0, 10);
        assertEquals(1, byOrigin.data().size());

        // 9. Find By Destination Paginated
        var byDest = routeRepository.findByDestination(new IataCode("OPO"), 0, 10);
        assertEquals(1, byDest.data().size());

        // 10. Find By Origin and Destination Paginated
        var byOriginDest = routeRepository.findByOriginAndDestination(new IataCode("LIS"), new IataCode("OPO"), 0, 10);
        assertEquals(1, byOriginDest.data().size());

        // 11. Find By Origin or Destination
        List<Route> orResults = routeRepository.findByOriginOrDestination(new IataCode("LIS"), new IataCode("OPO"));
        assertEquals(1, orResults.size());

        // 12. Find Compatible Routes
        List<Route> compatible = routeRepository.findCompatibleRoutes(400.0, 200);
        assertEquals(1, compatible.size());

        // 13. List Summaries
        List<RouteSummaryData> summaries = routeRepository.listSummariesForAirport(new IataCode("LIS"));
        assertEquals(1, summaries.size());
        assertEquals("LIS", summaries.get(0).origin().getCode());

        // 14. Delete
        routeRepository.delete(route);
        assertEquals(0, routeRepository.count());
    }
}
