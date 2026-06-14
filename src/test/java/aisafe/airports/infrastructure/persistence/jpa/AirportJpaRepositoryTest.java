package aisafe.airports.infrastructure.persistence.jpa;

import aisafe.airports.domain.AirportStatisticsData;
import aisafe.routes.domain.RouteStatus;
import aisafe.routes.infrastructure.persistence.jpa.RouteJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import aisafe.airports.domain.Airport;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("jpa")
@Import(AirportJpaRepository.class)
class AirportJpaRepositoryTest {

    @Autowired
    private AirportJpaRepository repository;

    @Autowired
    private TestEntityManager em;

    private AirportJpaEntity buildAirport(String iataCode, String country, String region) {
        AirportJpaEntity e = new AirportJpaEntity();
        e.setIataCode(iataCode);
        e.setName(iataCode + " Airport");
        e.setCity("City");
        e.setCountry(country);
        e.setRegion(region);
        e.setTimezone("UTC");
        e.setStatus("OPERATIONAL");
        e.setLatitude(0.0);
        e.setLongitude(0.0);
        e.getRunways().add(new RunwayEmbeddable("01/19", 2500, "010/190"));
        return e;
    }

    private RouteJpaEntity buildRoute(String origin, String destination) {
        return new RouteJpaEntity(origin, destination, 60, 500.0, 100, RouteStatus.ACTIVE);
    }

    @Test
    void ensureFindStatisticsReturnsCorrectRouteCounts() {
        em.persist(buildAirport("LIS", "Portugal", "Europe"));
        em.persist(buildAirport("OPO", "Portugal", "Europe"));
        em.persist(buildRoute("LIS", "OPO"));
        em.persist(buildRoute("OPO", "LIS"));
        em.persist(buildRoute("LIS", "MAD"));
        em.flush();

        List<AirportStatisticsData> result = repository.findStatistics();

        assertEquals(2, result.size());
        assertEquals("LIS", result.get(0).iataCode());
        assertEquals(3L, result.get(0).routeCount());
        assertEquals("OPO", result.get(1).iataCode());
        assertEquals(2L, result.get(1).routeCount());
    }

    @Test
    void ensureFindStatisticsWithNoRoutesReturnsZeroCount() {
        em.persist(buildAirport("CDG", "France", "Europe"));
        em.flush();

        List<AirportStatisticsData> result = repository.findStatistics();

        assertEquals(1, result.size());
        assertEquals("CDG", result.get(0).iataCode());
        assertEquals(0L, result.get(0).routeCount());
    }

    @Test
    void ensureFindAllOrderedByRegionReturnsSortedList() {
        em.persist(buildAirport("LIS", "Portugal", "Europe"));
        em.persist(buildAirport("SIN", "Singapore", "Asia"));
        em.persist(buildAirport("CDG", "France", null));
        em.flush();

        List<Airport> result = repository.findAllOrderedByRegion();

        assertEquals(3, result.size());
        assertEquals("SIN", result.get(0).getIataCode().getCode());
        assertEquals("LIS", result.get(1).getIataCode().getCode());
        assertEquals("CDG", result.get(2).getIataCode().getCode());
    }

    @Test
    void ensureFindAllOrderedByCountryReturnsSortedList() {
        em.persist(buildAirport("LIS", "Portugal", "Europe"));
        em.persist(buildAirport("CDG", "France", "Europe"));
        em.persist(buildAirport("JFK", "USA", "North America"));
        em.flush();

        List<Airport> result = repository.findAllOrderedByCountry();

        assertEquals(3, result.size());
        assertEquals("CDG", result.get(0).getIataCode().getCode());
        assertEquals("LIS", result.get(1).getIataCode().getCode());
        assertEquals("JFK", result.get(2).getIataCode().getCode());
    }
}
