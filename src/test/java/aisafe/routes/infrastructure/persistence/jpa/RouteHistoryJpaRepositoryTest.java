package aisafe.routes.infrastructure.persistence.jpa;

import aisafe.airports.domain.IataCode;
import aisafe.routes.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("jpa")
@Transactional
class RouteHistoryJpaRepositoryTest {

    @Autowired
    private RouteHistoryRepository historyRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private SpringDataRouteHistoryRepository springHistoryRepo;

    @Test
    void ensureSaveAndFindAllAndCount() {
        Route route = new Route("OPO", "LIS", 60, 300.0, 100);
        routeRepository.save(route);

        LocalDateTime now = LocalDateTime.now();
        RouteHistory history = new RouteHistory(
                new IataCode("OPO"),
                new IataCode("LIS"),
                "Route Created",
                now,
                "admin"
        );

        historyRepository.save(history);
        assertEquals(1, historyRepository.count());

        List<RouteHistory> all = historyRepository.findAll();
        assertEquals(1, all.size());
        assertEquals("Route Created", all.get(0).getChangeDescription());
        assertEquals("admin", all.get(0).getChangedBy());
    }

    @Test
    void ensureSaveThrowsRouteNotFoundExceptionWhenRouteDoesNotExist() {
        RouteHistory history = new RouteHistory(
                new IataCode("XYZ"),
                new IataCode("LIS"),
                "Route Created",
                LocalDateTime.now(),
                "admin"
        );

        assertThrows(RouteNotFoundException.class, () -> historyRepository.save(history));
    }

    @Test
    void ensureFindAllByRouteAndClear() {
        Route route = new Route("OPO", "LIS", 60, 300.0, 100);
        routeRepository.save(route);

        RouteHistory history1 = new RouteHistory(
                new IataCode("OPO"),
                new IataCode("LIS"),
                "First change",
                LocalDateTime.now().minusHours(1),
                "admin"
        );
        RouteHistory history2 = new RouteHistory(
                new IataCode("OPO"),
                new IataCode("LIS"),
                "Second change",
                LocalDateTime.now(),
                "admin"
        );

        historyRepository.save(history1);
        historyRepository.save(history2);

        List<RouteHistory> historyList = historyRepository.findAllByRoute("OPO", "LIS");
        assertEquals(2, historyList.size());

        historyRepository.deleteAllByRoute("OPO", "LIS");
        assertEquals(0, historyRepository.findAllByRoute("OPO", "LIS").size());
    }

    @Test
    void ensureDelete() {
        Route route = new Route("OPO", "LIS", 60, 300.0, 100);
        routeRepository.save(route);

        LocalDateTime changeTime = LocalDateTime.now().minusHours(1);
        RouteHistory history = new RouteHistory(
                new IataCode("OPO"),
                new IataCode("LIS"),
                "First change",
                changeTime,
                "admin"
        );

        historyRepository.save(history);
        assertEquals(1, historyRepository.count());

        historyRepository.delete(history);
        assertEquals(0, historyRepository.count());
    }

    @Test
    void ensureRouteHistoryJpaEntityGettersAndSetters() {
        RouteHistoryJpaEntity entity = new RouteHistoryJpaEntity();
        entity.setId(10L);
        entity.setChangeDescription("Desc");
        entity.setChangedAt(LocalDateTime.now());
        entity.setChangedBy("user");

        assertEquals(10L, entity.getId());
        assertEquals("Desc", entity.getChangeDescription());
        assertNotNull(entity.getChangedAt());
        assertEquals("user", entity.getChangedBy());
    }

    @Test
    void ensureRouteJpaEntityCoverage() {
        RouteJpaEntity entity = new RouteJpaEntity();
        entity.setId(5L);
        entity.setOriginCode(new IataCodeJpaEmbeddable("LIS"));
        entity.setDestinationCode(new IataCodeJpaEmbeddable("OPO"));
        entity.setEstimatedFlightTime(50);
        entity.setMinimumRange(400.0);
        entity.setMinimumCapacity(120);
        entity.setStatus(RouteStatus.ACTIVE);
        entity.setVersion(1L);

        assertEquals(5L, entity.getId());
        assertEquals("LIS", entity.getOriginCode().getCode());
        assertEquals("OPO", entity.getDestinationCode().getCode());
        assertEquals(50, entity.getEstimatedFlightTime());
        assertEquals(400.0, entity.getMinimumRange());
        assertEquals(120, entity.getMinimumCapacity());
        assertEquals(RouteStatus.ACTIVE, entity.getStatus());
        assertEquals(1L, entity.getVersion());
    }

    @Test
    void ensureIataCodeJpaEmbeddableCoverage() {
        IataCodeJpaEmbeddable code = new IataCodeJpaEmbeddable();
        code.setCode("MAD");
        assertEquals("MAD", code.getCode());
    }

    @Test
    void ensureDeleteDoesNothingWhenRouteHistoryDoesNotExist() {
        Route route = new Route("OPO", "LIS", 60, 300.0, 100);
        routeRepository.save(route);

        RouteHistory nonExistent = new RouteHistory(
                new IataCode("OPO"),
                new IataCode("LIS"),
                "Non-existent change",
                LocalDateTime.now().minusDays(1),
                "non-existent-user"
        );

        long initialCount = historyRepository.count();
        assertDoesNotThrow(() -> historyRepository.delete(nonExistent));
        assertEquals(initialCount, historyRepository.count());
    }

    @Test
    void ensureDeleteDoesNothingWhenChangedByMismatches() {
        Route route = new Route("OPO", "LIS", 60, 300.0, 100);
        routeRepository.save(route);

        LocalDateTime changeTime = LocalDateTime.now().minusHours(1);
        RouteHistory history = new RouteHistory(
                new IataCode("OPO"),
                new IataCode("LIS"),
                "First change",
                changeTime,
                "admin"
        );
        historyRepository.save(history);

        RouteHistory toDelete = new RouteHistory(
                new IataCode("OPO"),
                new IataCode("LIS"),
                "First change",
                changeTime,
                "user"
        );

        historyRepository.delete(toDelete);
        assertEquals(1, historyRepository.count());
    }

    @Test
    void ensureDeleteDoesNothingWhenChangedAtMismatches() {
        Route route = new Route("OPO", "LIS", 60, 300.0, 100);
        routeRepository.save(route);

        LocalDateTime changeTime = LocalDateTime.now().minusHours(1);
        RouteHistory history = new RouteHistory(
                new IataCode("OPO"),
                new IataCode("LIS"),
                "First change",
                changeTime,
                "admin"
        );
        historyRepository.save(history);

        RouteHistory toDelete = new RouteHistory(
                new IataCode("OPO"),
                new IataCode("LIS"),
                "First change",
                changeTime.plusMinutes(1),
                "admin"
        );

        historyRepository.delete(toDelete);
        assertEquals(1, historyRepository.count());
    }

    @Test
    void ensureMappersPrivateConstructors() throws Exception {
        // Instantiate RouteMapper using reflection to get 100% coverage
        java.lang.reflect.Constructor<RouteMapper> routeMapperConstructor = RouteMapper.class.getDeclaredConstructor();
        routeMapperConstructor.setAccessible(true);
        assertThrows(java.lang.reflect.InvocationTargetException.class, routeMapperConstructor::newInstance);

        // Instantiate RouteHistoryMapper using reflection
        java.lang.reflect.Constructor<RouteHistoryMapper> routeHistoryMapperConstructor = RouteHistoryMapper.class.getDeclaredConstructor();
        routeHistoryMapperConstructor.setAccessible(true);
        assertThrows(java.lang.reflect.InvocationTargetException.class, routeHistoryMapperConstructor::newInstance);
    }
}
