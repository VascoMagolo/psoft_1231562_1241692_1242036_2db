package aisafe.airports.infrastructure.persistence.jpa;

import aisafe.aircrafts.domain.ModelName;
import aisafe.airports.domain.*;
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
@Import(AircraftCertificationJpaRepository.class)
class AircraftCertificationJpaRepositoryTest {

    @Autowired
    private AircraftCertificationJpaRepository repository;

    @Autowired
    private TestEntityManager em;

    private AirportJpaEntity buildAirport(String iataCode) {
        AirportJpaEntity e = new AirportJpaEntity();
        e.setIataCode(new IataCodeJpaEmbeddable(iataCode));
        e.setName(iataCode + " Airport");
        e.setCity("City");
        e.setCountry("Country");
        e.setRegion("Region");
        e.setTimezone("UTC");
        e.setStatus("OPERATIONAL");
        e.setCoordinates(new CoordinatesJpaEmbeddable(0.0, 0.0));
        return e;
    }

    @Test
    void ensureSaveAndFindAllAndCount() {
        AirportJpaEntity airportEntity = buildAirport("LIS");
        em.persist(airportEntity);
        em.flush();

        AircraftCertification domainCert = new AircraftCertification(new IataCode("LIS"), new ModelName("A320"));
        repository.save(domainCert);

        assertEquals(1, repository.count());
        List<AircraftCertification> all = repository.findAll();
        assertEquals(1, all.size());
        assertEquals("LIS", all.get(0).getAirportCode().getCode());
        assertEquals("A320", all.get(0).getAircraftModelName().getName());
    }

    @Test
    void ensureFindByAirportCode() {
        AirportJpaEntity airportEntity = buildAirport("OPO");
        em.persist(airportEntity);
        em.flush();

        AircraftCertification domainCert = new AircraftCertification(new IataCode("OPO"), new ModelName("B737"));
        repository.save(domainCert);

        List<AircraftCertification> certs = repository.findByAirportCode(new IataCode("OPO"));
        assertEquals(1, certs.size());
        assertEquals("B737", certs.get(0).getAircraftModelName().getName());
    }

    @Test
    void ensureFindByAirportCodeThrowsWhenAirportNotFound() {
        assertThrows(AirportNotFoundException.class, () -> repository.findByAirportCode(new IataCode("XYZ")));
    }

    @Test
    void ensureExistsByAirportCodeAndAircraftModelName() {
        AirportJpaEntity airportEntity = buildAirport("JFK");
        em.persist(airportEntity);
        em.flush();

        AircraftCertification domainCert = new AircraftCertification(new IataCode("JFK"), new ModelName("A350"));
        repository.save(domainCert);

        assertTrue(repository.existsByAirportCodeAndAircraftModelName(new IataCode("JFK"), new ModelName("A350")));
        assertFalse(repository.existsByAirportCodeAndAircraftModelName(new IataCode("JFK"), new ModelName("A380")));
        assertFalse(repository.existsByAirportCodeAndAircraftModelName(new IataCode("LAX"), new ModelName("A350")));
    }

    @Test
    void ensureSaveThrowsWhenAirportNotFound() {
        AircraftCertification domainCert = new AircraftCertification(new IataCode("XYZ"), new ModelName("A320"));
        assertThrows(AirportNotFoundException.class, () -> repository.save(domainCert));
    }

    @Test
    void ensureDelete() {
        AirportJpaEntity airportEntity = buildAirport("LIS");
        em.persist(airportEntity);
        em.flush();

        AircraftCertification domainCert = new AircraftCertification(new IataCode("LIS"), new ModelName("A320"));
        repository.save(domainCert);
        assertEquals(1, repository.count());

        repository.delete(domainCert);
        assertEquals(0, repository.count());
    }

    @Test
    void ensureDeleteThrowsWhenAirportNotFound() {
        AircraftCertification domainCert = new AircraftCertification(new IataCode("XYZ"), new ModelName("A320"));
        assertThrows(AirportNotFoundException.class, () -> repository.delete(domainCert));
    }

    @Test
    void ensureMapperReturnsNullForNullEntity() {
        assertNull(AircraftCertificationMapper.toDomain(null));
    }
}
