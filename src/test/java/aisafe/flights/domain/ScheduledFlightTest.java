package aisafe.flights.domain;

import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.routes.domain.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ScheduledFlightTest {

    @Mock
    private Route mockRoute;
    @Mock
    private RegistrationNumber mockRegistrationNumber;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void ensureScheduledFlightIsCreatedCorrectly() {
        OffsetDateTime departure = OffsetDateTime.now();
        OffsetDateTime arrival = departure.plusHours(2);

        ScheduledFlight flight = new ScheduledFlight(departure, arrival, FlightStatus.SCHEDULED, mockRoute, mockRegistrationNumber);

        assertNotNull(flight);
        assertEquals(departure, flight.getDepartureDateTime());
        assertEquals(arrival, flight.getArrivalDateTime());
        assertEquals(FlightStatus.SCHEDULED, flight.getStatus());
        assertEquals(mockRoute, flight.getRoute());
        assertEquals(mockRegistrationNumber, flight.getAircraftRegistrationNumber());
        assertNull(flight.getId());
    }

    @Test
    void ensureSetIdWorksCorrectly() {
        OffsetDateTime departure = OffsetDateTime.now();
        OffsetDateTime arrival = departure.plusHours(2);
        ScheduledFlight flight = new ScheduledFlight(departure, arrival, FlightStatus.SCHEDULED, mockRoute, mockRegistrationNumber);
        Long expectedId = 123L;
        flight.setId(expectedId);
        assertEquals(expectedId, flight.getId());
    }

    @Test
    void ensureGetDurationCalculatesCorrectly() {
        OffsetDateTime departure = OffsetDateTime.now();
        OffsetDateTime arrival = departure.plusHours(2);
        ScheduledFlight flight = new ScheduledFlight(departure, arrival, FlightStatus.SCHEDULED, mockRoute, mockRegistrationNumber);
        assertEquals(Duration.ofHours(2), flight.getDuration());
    }

    @Test
    void ensureGetDurationReturnsZeroIfDatesAreNull() {
        // This scenario is prevented by constructor validation, but good to test for robustness
        ScheduledFlight flight = new ScheduledFlight(
                OffsetDateTime.now(), OffsetDateTime.now().plusHours(1), FlightStatus.SCHEDULED, mockRoute, mockRegistrationNumber
        );
        // Using reflection to set private fields to null for this specific test case, as constructor prevents it.
        // In a real scenario, consider if this state is actually reachable.
        try {
            java.lang.reflect.Field departureField = ScheduledFlight.class.getDeclaredField("departureDateTime");
            departureField.setAccessible(true);
            departureField.set(flight, null);

            java.lang.reflect.Field arrivalField = ScheduledFlight.class.getDeclaredField("arrivalDateTime");
            arrivalField.setAccessible(true);
            arrivalField.set(flight, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set fields to null via reflection: " + e.getMessage());
        }
        assertEquals(Duration.ZERO, flight.getDuration());
    }

    @Test
    void ensureConstructorThrowsExceptionForNullDepartureDateTime() {
        OffsetDateTime arrival = OffsetDateTime.now().plusHours(2);
        assertThrows(InvalidFlightScheduleException.class, () ->
                new ScheduledFlight(null, arrival, FlightStatus.SCHEDULED, mockRoute, mockRegistrationNumber));
    }

    @Test
    void ensureConstructorThrowsExceptionForNullArrivalDateTime() {
        OffsetDateTime departure = OffsetDateTime.now();
        assertThrows(InvalidFlightScheduleException.class, () ->
                new ScheduledFlight(departure, null, FlightStatus.SCHEDULED, mockRoute, mockRegistrationNumber));
    }

    @Test
    void ensureConstructorThrowsExceptionForNullStatus() {
        OffsetDateTime departure = OffsetDateTime.now();
        OffsetDateTime arrival = departure.plusHours(2);
        assertThrows(InvalidFlightScheduleException.class, () ->
                new ScheduledFlight(departure, arrival, null, mockRoute, mockRegistrationNumber));
    }

    @Test
    void ensureConstructorThrowsExceptionForNullRoute() {
        OffsetDateTime departure = OffsetDateTime.now();
        OffsetDateTime arrival = departure.plusHours(2);
        assertThrows(InvalidFlightScheduleException.class, () ->
                new ScheduledFlight(departure, arrival, FlightStatus.SCHEDULED, null, mockRegistrationNumber));
    }

    @Test
    void ensureConstructorThrowsExceptionForNullAircraftRegistrationNumber() {
        OffsetDateTime departure = OffsetDateTime.now();
        OffsetDateTime arrival = departure.plusHours(2);
        assertThrows(InvalidFlightScheduleException.class, () ->
                new ScheduledFlight(departure, arrival, FlightStatus.SCHEDULED, mockRoute, null));
    }

    @Test
    void ensureConstructorThrowsExceptionWhenDepartureIsAfterArrival() {
        OffsetDateTime departure = OffsetDateTime.now().plusHours(2);
        OffsetDateTime arrival = OffsetDateTime.now();
        assertThrows(InvalidFlightScheduleException.class, () ->
                new ScheduledFlight(departure, arrival, FlightStatus.SCHEDULED, mockRoute, mockRegistrationNumber));
    }

    @Test
    void ensureConstructorThrowsExceptionWhenDepartureIsSameAsArrival() {
        OffsetDateTime departure = OffsetDateTime.now();
        OffsetDateTime arrival = departure;
        assertThrows(InvalidFlightScheduleException.class, () ->
                new ScheduledFlight(departure, arrival, FlightStatus.SCHEDULED, mockRoute, mockRegistrationNumber));
    }
}
