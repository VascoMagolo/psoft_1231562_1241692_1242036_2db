package aisafe.flights.domain;

import aisafe.shared.domain.DomainException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FlightTest {

    @Test
    void ensureValidFlightIsCreated() {
        OffsetDateTime departure = OffsetDateTime.now();
        OffsetDateTime arrival = departure.plusHours(2);
        Flight flight = new Flight("CS-TPA", 1L, departure, arrival);

        assertEquals("CS-TPA", flight.getAircraftRegistrationNumber());
        assertEquals(1L, flight.getRouteId());
        assertEquals(departure, flight.getDepartureDateTime());
        assertEquals(arrival, flight.getArrivalDateTime());
    }

    @Test
    void ensureNullRegistrationThrowsException() {
        OffsetDateTime departure = OffsetDateTime.now();
        OffsetDateTime arrival = departure.plusHours(2);
        assertThrows(DomainException.class, () -> new Flight(null, 1L, departure, arrival));
    }

    @Test
    void ensureArrivalBeforeDepartureThrowsException() {
        OffsetDateTime departure = OffsetDateTime.now();
        OffsetDateTime arrival = departure.minusHours(1);
        assertThrows(DomainException.class, () -> new Flight("CS-TPA", 1L, departure, arrival));
    }
}
