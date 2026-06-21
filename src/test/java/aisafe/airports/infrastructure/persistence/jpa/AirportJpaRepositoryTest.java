package aisafe.airports.infrastructure.persistence.jpa;

import aisafe.airports.domain.*;
import aisafe.routes.domain.RouteStatus;
import aisafe.routes.infrastructure.persistence.jpa.RouteJpaEntity;
import aisafe.shared.domain.PaginatedResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

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
        e.setIataCode(new IataCodeJpaEmbeddable(iataCode));
        e.setName(iataCode + " Airport");
        e.setCity("City");
        e.setCountry(country);
        e.setRegion(region);
        e.setTimezone("UTC");
        e.setStatus("OPERATIONAL");
        e.setCoordinates(new CoordinatesJpaEmbeddable(0.0, 0.0));
        e.getRunways().add(new RunwayJpaEmbeddable("01/19", 2500, "010/190"));
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
    void ensureFindAllGroupingByRegionReturnsSortedRows() {
        em.persist(buildAirport("LIS", "Portugal", "Europe"));
        em.persist(buildAirport("SIN", "Singapore", "Asia"));
        em.persist(buildAirport("CDG", "France", null));
        em.flush();

        List<AirportGroupingData> result = repository.findAllGroupingByRegion();

        assertEquals(3, result.size());
        assertEquals("SIN", result.get(0).iataCode().getCode());
        assertEquals("Asia", result.get(0).region());
        assertEquals("LIS", result.get(1).iataCode().getCode());
        assertEquals("Europe", result.get(1).region());
        assertEquals("CDG", result.get(2).iataCode().getCode());
        assertNull(result.get(2).region());
    }

    @Test
    void ensureFindAllGroupingByCountryReturnsSortedRows() {
        em.persist(buildAirport("LIS", "Portugal", "Europe"));
        em.persist(buildAirport("CDG", "France", "Europe"));
        em.persist(buildAirport("JFK", "USA", "North America"));
        em.flush();

        List<AirportGroupingData> result = repository.findAllGroupingByCountry();

        assertEquals(3, result.size());
        assertEquals("CDG", result.get(0).iataCode().getCode());
        assertEquals("France", result.get(0).country());
        assertEquals("LIS", result.get(1).iataCode().getCode());
        assertEquals("Portugal", result.get(1).country());
        assertEquals("JFK", result.get(2).iataCode().getCode());
        assertEquals("USA", result.get(2).country());
    }

    private Airport buildDomainAirport(String iataCode) {
        return new Airport(iataCode, "Name", "City", "Country", "Region", "UTC", 0.0, 0.0,
                List.of(new Runway("03/21", 3000, "030/210")));
    }

    @Test
    void ensureDomainRepositorySaveAndCountAndExists() {
        long initialCount = repository.count();
        Airport airport = buildDomainAirport("MAD");
        
        repository.save(airport);
        
        assertEquals(initialCount + 1, repository.count());
        assertTrue(repository.existsByIataCode(new IataCode("MAD")));
        assertFalse(repository.existsByIataCode(new IataCode("BCN")));
    }

    @Test
    void ensureDomainRepositoryFindMethods() {
        Airport airport = buildDomainAirport("CDG");
        repository.save(airport);

        var found = repository.findByIataCode(new IataCode("CDG"));
        assertTrue(found.isPresent());
        assertEquals("CDG", found.get().getIataCode().getCode());

        var all = repository.findAll();
        assertTrue(all.stream().anyMatch(a -> a.getIataCode().getCode().equals("CDG")));
    }

    @Test
    void ensureDomainRepositorySearchAirports() {
        Airport airport = buildDomainAirport("JFK");
        repository.save(airport);

        PaginatedResult<Airport> paginated = repository.searchAirports("Name", "City", "Country", 0, 10);
        assertNotNull(paginated);
        assertTrue(paginated.totalElements() > 0);
        assertTrue(paginated.data().stream().anyMatch(a -> a.getIataCode().getCode().equals("JFK")));
    }

    @Test
    void ensureDomainRepositoryFindVersionFor() {
        Airport airport = buildDomainAirport("OPO");
        repository.save(airport);

        Long version = repository.findVersionFor(new IataCode("OPO"));
        assertNotNull(version);

        Long nonExistentVersion = repository.findVersionFor(new IataCode("NEX"));
        assertEquals(0L, nonExistentVersion);
    }

    @Test
    void ensureDomainRepositoryDelete() {
        Airport airport = buildDomainAirport("LIS");
        repository.save(airport);
        assertTrue(repository.existsByIataCode(new IataCode("LIS")));

        repository.delete(airport);
        assertFalse(repository.existsByIataCode(new IataCode("LIS")));
    }

    @Test
    void ensureDomainRepositoryDeleteThrowsWhenNotFound() {
        Airport airport = buildDomainAirport("LIS");
        assertThrows(AirportNotFoundException.class, () -> repository.delete(airport));
    }

    @Test
    void ensureDomainRepositorySaveExistingAirport() {
        Airport airport = buildDomainAirport("MAD");
        repository.save(airport); // first save

        // modify something and save again
        Airport retrieved = repository.findByIataCode(new IataCode("MAD")).orElseThrow();
        retrieved.updateDetails("12h", List.of(), List.of(), List.of(), List.of(), List.of());
        repository.save(retrieved); // second save of existing airport

        Airport finalRetrieved = repository.findByIataCode(new IataCode("MAD")).orElseThrow();
        assertEquals("12h", finalRetrieved.getOperationalHours());
    }
}
