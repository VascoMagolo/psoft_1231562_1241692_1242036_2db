package aisafe.airports.domain;

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
        assertThrows(IllegalArgumentException.class, () ->
                new Airport("LIS", "  ", "Lisbon", "Portugal", "Europe", "Europe/Lisbon", 38.77, -9.13, oneRunway()));
    }

    @Test
    void ensureBlankCityThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Airport("LIS", "Lisbon Airport", "", "Portugal", "Europe", "Europe/Lisbon", 38.77, -9.13, oneRunway()));
    }

    @Test
    void ensureBlankCountryThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Airport("LIS", "Lisbon Airport", "Lisbon", "  ", "Europe", "Europe/Lisbon", 38.77, -9.13, oneRunway()));
    }

    @Test
    void ensureBlankTimezoneThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Airport("LIS", "Lisbon Airport", "Lisbon", "Portugal", "Europe", "", 38.77, -9.13, oneRunway()));
    }

    @Test
    void ensureEmptyRunwaysThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Airport("LIS", "Lisbon Airport", "Lisbon", "Portugal", "Europe", "Europe/Lisbon", 38.77, -9.13, List.of()));
    }

    @Test
    void ensureNullRunwaysThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Airport("LIS", "Lisbon Airport", "Lisbon", "Portugal", "Europe", "Europe/Lisbon", 38.77, -9.13, null));
    }

    @Test
    void ensureRegionIsOptional() {
        assertDoesNotThrow(() ->
                new Airport("LIS", "Lisbon Airport", "Lisbon", "Portugal", null, "Europe/Lisbon", 38.77, -9.13, oneRunway()));
    }
}
