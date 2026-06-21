package aisafe.airports.application.dtos;

import aisafe.airports.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AirportResponseTest {

    @Test
    void ensureCoordinatesRecordBehavesCorrectly() {
        AirportResponse.CoordinatesRecord record1 = new AirportResponse.CoordinatesRecord(38.77, -9.13);
        AirportResponse.CoordinatesRecord record2 = new AirportResponse.CoordinatesRecord(38.77, -9.13);
        AirportResponse.CoordinatesRecord record3 = new AirportResponse.CoordinatesRecord(40.0, -8.0);

        assertEquals(38.77, record1.latitude());
        assertEquals(-9.13, record1.longitude());
        assertEquals(record1, record2);
        assertNotEquals(record1, record3);
        assertEquals(record1.hashCode(), record2.hashCode());
        assertNotNull(record1.toString());
    }

    @Test
    void ensureRunwayRecordBehavesCorrectly() {
        AirportResponse.RunwayRecord record1 = new AirportResponse.RunwayRecord("03/21", 3000, "030/210");
        AirportResponse.RunwayRecord record2 = new AirportResponse.RunwayRecord("03/21", 3000, "030/210");
        AirportResponse.RunwayRecord record3 = new AirportResponse.RunwayRecord("08/26", 2500, "080/260");

        assertEquals("03/21", record1.name());
        assertEquals(3000, record1.length());
        assertEquals("030/210", record1.orientation());
        assertEquals(record1, record2);
        assertNotEquals(record1, record3);
        assertEquals(record1.hashCode(), record2.hashCode());
        assertNotNull(record1.toString());
    }

    @Test
    void ensureContactRecordBehavesCorrectly() {
        AirportResponse.ContactRecord record1 = new AirportResponse.ContactRecord("EMAIL", "info@lisbon.com", "Info");
        AirportResponse.ContactRecord record2 = new AirportResponse.ContactRecord("EMAIL", "info@lisbon.com", "Info");
        AirportResponse.ContactRecord record3 = new AirportResponse.ContactRecord("PHONE", "123456", "Phone");

        assertEquals("EMAIL", record1.type());
        assertEquals("info@lisbon.com", record1.value());
        assertEquals("Info", record1.description());
        assertEquals(record1, record2);
        assertNotEquals(record1, record3);
        assertEquals(record1.hashCode(), record2.hashCode());
        assertNotNull(record1.toString());
    }

    @Test
    void ensureAirportResponseMappingAndMethodsBehaveCorrectly() {
        Airport airport = new Airport("LIS", "Lisbon Airport", "Lisbon", "Portugal", "Europe", "Europe/Lisbon",
                38.77, -9.13, List.of(new Runway("03/21", 3000, "030/210")));
        airport.updateDetails("06:00-23:00",
                List.of(new Contact(ContactType.EMAIL, "info@lisbon.com", "Info")),
                null,
                List.of(new Service("WiFi")),
                List.of(new Terminal("T1")),
                List.of(new Gate("A1")));

        AirportResponse response = AirportResponse.from(airport, 1L);

        assertEquals("LIS", response.iataCode());
        assertEquals("Lisbon Airport", response.name());
        assertEquals("Lisbon", response.city());
        assertEquals("Portugal", response.country());
        assertEquals("Europe", response.region());
        assertEquals("Europe/Lisbon", response.timezone());
        assertEquals(0, response.photoCount());
        assertEquals("06:00-23:00", response.operationalHours());
        assertEquals("OPERATIONAL", response.status());
        assertEquals(1L, response.version());

        assertEquals(38.77, response.coordinates().latitude());
        assertEquals(-9.13, response.coordinates().longitude());

        assertEquals(1, response.runways().size());
        assertEquals("03/21", response.runways().get(0).name());
        assertEquals(3000, response.runways().get(0).length());
        assertEquals("030/210", response.runways().get(0).orientation());

        assertEquals(1, response.contacts().size());
        assertEquals("EMAIL", response.contacts().get(0).type());
        assertEquals("info@lisbon.com", response.contacts().get(0).value());
        assertEquals("Info", response.contacts().get(0).description());

        assertEquals(List.of("WiFi"), response.services());
        assertEquals(List.of("T1"), response.terminals());
        assertEquals(List.of("A1"), response.gates());

        AirportResponse responseWithoutVersion = AirportResponse.from(airport);
        assertNull(responseWithoutVersion.version());

        assertNotNull(response.toString());
        assertEquals(response, AirportResponse.from(airport, 1L));
        assertEquals(response.hashCode(), AirportResponse.from(airport, 1L).hashCode());
    }
}
