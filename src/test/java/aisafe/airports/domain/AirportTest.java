package aisafe.airports.domain;

import aisafe.shared.domain.DomainException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AirportTest {

    private List<Runway> oneRunway() {
        return List.of(new Runway("03/21", 3000, "030/210"));
    }

    @Test
    void ensureValidAirportCreatedWithOperationalStatus() {
        Airport airport = new Airport("LIS", "Lisbon Airport", "Lisbon", "Portugal",
                "Europe", "Europe/Lisbon", 38.77, -9.13, oneRunway());

        assertEquals(AirportStatus.OPERATIONAL, airport.getStatus());
        assertEquals("LIS", airport.getIataCode().getCode());
        assertEquals("Lisbon Airport", airport.getName());
    }

    @Test
    void ensureBlankNameThrowsException() {
        assertThrows(DomainException.class, () ->
                new Airport("LIS", "  ", "Lisbon", "Portugal", "Europe", "Europe/Lisbon", 38.77, -9.13, oneRunway()));
    }

    @Test
    void ensureBlankCityThrowsException() {
        assertThrows(DomainException.class, () ->
                new Airport("LIS", "Lisbon Airport", "", "Portugal", "Europe", "Europe/Lisbon", 38.77, -9.13, oneRunway()));
    }

    @Test
    void ensureBlankCountryThrowsException() {
        assertThrows(DomainException.class, () ->
                new Airport("LIS", "Lisbon Airport", "Lisbon", "  ", "Europe", "Europe/Lisbon", 38.77, -9.13, oneRunway()));
    }

    @Test
    void ensureBlankTimezoneThrowsException() {
        assertThrows(DomainException.class, () ->
                new Airport("LIS", "Lisbon Airport", "Lisbon", "Portugal", "Europe", "", 38.77, -9.13, oneRunway()));
    }

    @Test
    void ensureEmptyRunwaysThrowsException() {
        assertThrows(DomainException.class, () ->
                new Airport("LIS", "Lisbon Airport", "Lisbon", "Portugal", "Europe", "Europe/Lisbon", 38.77, -9.13, List.of()));
    }

    @Test
    void ensureNullRunwaysThrowsException() {
        assertThrows(DomainException.class, () ->
                new Airport("LIS", "Lisbon Airport", "Lisbon", "Portugal", "Europe", "Europe/Lisbon", 38.77, -9.13, null));
    }

    @Test
    void ensureRegionIsOptional() {
        assertDoesNotThrow(() ->
                new Airport("LIS", "Lisbon Airport", "Lisbon", "Portugal", null, "Europe/Lisbon", 38.77, -9.13, oneRunway()));
    }

    @Test
    void ensureNullConstructorArgumentsThrowExceptions() {
        assertThrows(DomainException.class, () ->
                new Airport("LIS", null, "Lisbon", "Portugal", "Europe", "Europe/Lisbon", 38.77, -9.13, oneRunway()));
        assertThrows(DomainException.class, () ->
                new Airport("LIS", "Lisbon", null, "Portugal", "Europe", "Europe/Lisbon", 38.77, -9.13, oneRunway()));
        assertThrows(DomainException.class, () ->
                new Airport("LIS", "Lisbon", "Lisbon", null, "Europe", "Europe/Lisbon", 38.77, -9.13, oneRunway()));
        assertThrows(DomainException.class, () ->
                new Airport("LIS", "Lisbon", "Lisbon", "Portugal", "Europe", null, 38.77, -9.13, oneRunway()));
    }

    @Test
    void ensureUpdateDetailsHandlesNullsAndValues() {
        Airport airport = new Airport("LIS", "Lisbon Airport", "Lisbon", "Portugal",
                "Europe", "Europe/Lisbon", 38.77, -9.13, oneRunway());

        assertDoesNotThrow(() -> airport.updateDetails(null, null, null, null, null, null));
        assertNull(airport.getOperationalHours());

        airport.updateDetails("08:00-20:00", List.of(), List.of(), List.of(), List.of(), List.of());
        assertEquals("08:00-20:00", airport.getOperationalHours());
    }
}
