package aisafe.routes.infrastructure.persistence.jpa;

import aisafe.flights.infrastructure.persistence.SpringDataScheduledFlightRepository;
import aisafe.routes.domain.RouteStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("jpa")
@Transactional
class RouteRepositoryTest {

    @Autowired
    private SpringDataRouteRepository springRepo;

    @Autowired
    private SpringDataScheduledFlightRepository flightRepo;

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
        assertEquals("XXX", result.get(0).getOriginCode());
        assertEquals("YYY", result.get(0).getDestinationCode());
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
}
