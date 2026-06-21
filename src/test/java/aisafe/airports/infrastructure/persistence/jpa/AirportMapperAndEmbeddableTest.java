package aisafe.airports.infrastructure.persistence.jpa;

import aisafe.airports.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AirportMapperAndEmbeddableTest {

    @Test
    void ensureAirportMapperNullCheck() {
        assertNull(AirportMapper.toDomain(null));
        assertNull(AirportMapper.toJpa(null));
    }

    @Test
    void ensureEmbeddablesGettersAndSetters() {
        // AirportPhotoJpaEmbeddable
        AirportPhotoJpaEmbeddable photo = new AirportPhotoJpaEmbeddable();
        byte[] bytes = {1, 2, 3};
        photo.setBytes(bytes);
        photo.setContentType("image/png");
        assertArrayEquals(bytes, photo.getBytes());
        assertEquals("image/png", photo.getContentType());

        // ContactJpaEmbeddable
        ContactJpaEmbeddable contact = new ContactJpaEmbeddable();
        contact.setType("EMAIL");
        contact.setValue("info@airport.com");
        contact.setDescription("General Inquiry");
        assertEquals("EMAIL", contact.getType());
        assertEquals("info@airport.com", contact.getValue());
        assertEquals("General Inquiry", contact.getDescription());

        ContactJpaEmbeddable contact2 = new ContactJpaEmbeddable("PHONE", "12345", "Emergency");
        assertEquals("PHONE", contact2.getType());
        assertEquals("12345", contact2.getValue());
        assertEquals("Emergency", contact2.getDescription());

        // CoordinatesJpaEmbeddable
        CoordinatesJpaEmbeddable coords = new CoordinatesJpaEmbeddable();
        coords.setLatitude(45.0);
        coords.setLongitude(-90.0);
        assertEquals(45.0, coords.getLatitude());
        assertEquals(-90.0, coords.getLongitude());

        CoordinatesJpaEmbeddable coords2 = new CoordinatesJpaEmbeddable(10.0, 20.0);
        assertEquals(10.0, coords2.getLatitude());
        assertEquals(20.0, coords2.getLongitude());

        // IataCodeJpaEmbeddable
        IataCodeJpaEmbeddable iata = new IataCodeJpaEmbeddable();
        iata.setCode("LIS");
        assertEquals("LIS", iata.getCode());

        IataCodeJpaEmbeddable iata2 = new IataCodeJpaEmbeddable("OPO");
        assertEquals("OPO", iata2.getCode());

        // RunwayJpaEmbeddable
        RunwayJpaEmbeddable runway = new RunwayJpaEmbeddable();
        runway.setName("09/27");
        runway.setLength(3500);
        runway.setOrientation("090/270");
        assertEquals("09/27", runway.getName());
        assertEquals(3500, runway.getLength());
        assertEquals("090/270", runway.getOrientation());

        RunwayJpaEmbeddable runway2 = new RunwayJpaEmbeddable("18/36", 2000, "180/360");
        assertEquals("18/36", runway2.getName());
        assertEquals(2000, runway2.getLength());
        assertEquals("180/360", runway2.getOrientation());
    }

    @Test
    void ensureAirportMapperRoundtrip() {
        Airport domainAirport = new Airport(
                "LIS", "Lisbon Airport", "Lisbon", "Portugal", "Europe", "Europe/Lisbon", 38.77, -9.13,
                List.of(new Runway("03/21", 3000, "030/210"))
        );
        domainAirport.updateDetails(
                "24h",
                List.of(new Contact(ContactType.EMAIL, "info@lisbon.com", "Info")),
                List.of(new AirportPhoto(new byte[]{1,2}, "image/jpeg")),
                List.of(new Service("WiFi")),
                List.of(new Terminal("T1")),
                List.of(new Gate("Gate 1"))
        );

        AirportJpaEntity jpaEntity = AirportMapper.toJpa(domainAirport);
        assertNotNull(jpaEntity);
        assertEquals("LIS", jpaEntity.getIataCode().getCode());
        assertEquals("Lisbon Airport", jpaEntity.getName());
        assertEquals("24h", jpaEntity.getOperationalHours());

        Airport domainMapped = AirportMapper.toDomain(jpaEntity);
        assertNotNull(domainMapped);
        assertEquals("LIS", domainMapped.getIataCode().getCode());
        assertEquals("Lisbon Airport", domainMapped.getName());
        assertEquals("24h", domainMapped.getOperationalHours());
        assertEquals(1, domainMapped.getContacts().size());
        assertEquals(1, domainMapped.getPhotos().size());
        assertEquals(1, domainMapped.getServices().size());
        assertEquals(1, domainMapped.getTerminals().size());
        assertEquals(1, domainMapped.getGates().size());
    }

    @Test
    void ensureAirportMapperFiltersNullPhotos() {
        AirportJpaEntity jpaEntity = new AirportJpaEntity();
        jpaEntity.setIataCode(new IataCodeJpaEmbeddable("LIS"));
        jpaEntity.setName("Lisbon Airport");
        jpaEntity.setCity("Lisbon");
        jpaEntity.setCountry("Portugal");
        jpaEntity.setRegion("Europe");
        jpaEntity.setTimezone("Europe/Lisbon");
        jpaEntity.setCoordinates(new CoordinatesJpaEmbeddable(38.77, -9.13));
        jpaEntity.setStatus("OPERATIONAL");
        jpaEntity.setOperationalHours("24h");
        jpaEntity.setRunways(List.of(new RunwayJpaEmbeddable("01/19", 2500, "010/190")));

        AirportPhotoJpaEmbeddable photo1 = new AirportPhotoJpaEmbeddable();
        photo1.setBytes(null); // this should be filtered out
        photo1.setContentType("image/png");

        AirportPhotoJpaEmbeddable photo2 = new AirportPhotoJpaEmbeddable();
        photo2.setBytes(new byte[]{1,2});
        photo2.setContentType("image/jpeg");

        jpaEntity.setPhotos(List.of(photo1, photo2));

        Airport domain = AirportMapper.toDomain(jpaEntity);
        assertNotNull(domain);
        assertEquals(1, domain.getPhotos().size());
        assertArrayEquals(new byte[]{1,2}, domain.getPhotos().get(0).getBytes());
    }

    @Test
    void ensureAircraftCertificationMapperRoundtrip() {
        AircraftCertificationJpaEntity entity = new AircraftCertificationJpaEntity();
        AirportJpaEntity airportEntity = new AirportJpaEntity();
        airportEntity.setIataCode(new IataCodeJpaEmbeddable("LIS"));
        entity.setAirport(airportEntity);
        entity.setAircraftModelName("A320");

        AircraftCertification domain = AircraftCertificationMapper.toDomain(entity);
        assertNotNull(domain);
        assertEquals("LIS", domain.getAirportCode().getCode());
        assertEquals("A320", domain.getAircraftModelName().getName());

        assertNull(AircraftCertificationMapper.toDomain(null));
    }

    @Test
    void ensureUtilityConstructorsArePrivate() throws Exception {
        java.lang.reflect.Constructor<AirportMapper> c1 = AirportMapper.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(c1.getModifiers()));
        c1.setAccessible(true);
        c1.newInstance();

        java.lang.reflect.Constructor<AircraftCertificationMapper> c2 = AircraftCertificationMapper.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(c2.getModifiers()));
        c2.setAccessible(true);
        c2.newInstance();
    }
}
