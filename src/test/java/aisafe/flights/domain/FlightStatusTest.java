package aisafe.flights.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FlightStatusTest {

    @Test
    void ensureAllEnumValuesExist() {
        assertNotNull(FlightStatus.SCHEDULED);
        assertNotNull(FlightStatus.DELAYED);
        assertNotNull(FlightStatus.IN_FLIGHT);
        assertNotNull(FlightStatus.COMPLETED);
        assertNotNull(FlightStatus.CANCELED);
    }

    @Test
    void ensureEnumValuesToStringAreCorrect() {
        assertEquals("SCHEDULED", FlightStatus.SCHEDULED.toString());
        assertEquals("DELAYED", FlightStatus.DELAYED.toString());
        assertEquals("IN_FLIGHT", FlightStatus.IN_FLIGHT.toString());
        assertEquals("COMPLETED", FlightStatus.COMPLETED.toString());
        assertEquals("CANCELED", FlightStatus.CANCELED.toString());
    }

    @Test
    void ensureValueOfReturnsCorrectEnum() {
        assertEquals(FlightStatus.SCHEDULED, FlightStatus.valueOf("SCHEDULED"));
        assertEquals(FlightStatus.DELAYED, FlightStatus.valueOf("DELAYED"));
        assertEquals(FlightStatus.IN_FLIGHT, FlightStatus.valueOf("IN_FLIGHT"));
        assertEquals(FlightStatus.COMPLETED, FlightStatus.valueOf("COMPLETED"));
        assertEquals(FlightStatus.CANCELED, FlightStatus.valueOf("CANCELED"));
    }

    @Test
    void ensureValueOfThrowsExceptionForInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> FlightStatus.valueOf("NON_EXISTENT"));
    }
}
