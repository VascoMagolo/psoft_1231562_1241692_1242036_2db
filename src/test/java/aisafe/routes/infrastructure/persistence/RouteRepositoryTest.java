package aisafe.routes.infrastructure.persistence;

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
}
