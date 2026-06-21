package aisafe.flights.domain;

import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.airports.domain.IataCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.time.Duration;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class ScheduledFlightTest {

    private final IataCode originCode = new IataCode("OPO");
    private final IataCode destinationCode = new IataCode("LIS");

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

        ScheduledFlight flight = new ScheduledFlight(departure, arrival, FlightStatus.SCHEDULED, originCode, destinationCode, mockRegistrationNumber);

        assertNotNull(flight);
        assertEquals(departure, flight.getDepartureDateTime());
        assertEquals(arrival, flight.getArrivalDateTime());
        assertEquals(FlightStatus.SCHEDULED, flight.getStatus());
        assertEquals(originCode, flight.getOriginCode());
        assertEquals(destinationCode, flight.getDestinationCode());
        assertEquals(mockRegistrationNumber, flight.getAircraftRegistrationNumber());
        assertNull(flight.getId());
    }

    @Test
    void ensureReconstitutionConstructorSetsId() {
        OffsetDateTime departure = OffsetDateTime.now();
        OffsetDateTime arrival = departure.plusHours(2);
        Long expectedId = 123L;
        ScheduledFlight flight = new ScheduledFlight(expectedId, departure, arrival, FlightStatus.SCHEDULED, originCode, destinationCode, mockRegistrationNumber);
        assertEquals(expectedId, flight.getId());
    }

    @Test
    void ensureGetDurationCalculatesCorrectly() {
        OffsetDateTime departure = OffsetDateTime.now();
        OffsetDateTime arrival = departure.plusHours(2);
        ScheduledFlight flight = new ScheduledFlight(departure, arrival, FlightStatus.SCHEDULED, originCode, destinationCode, mockRegistrationNumber);
        assertEquals(Duration.ofHours(2), flight.getDuration());
    }

    @Test
    void ensureGetDurationReturnsZeroIfDatesAreNull() {
        ScheduledFlight flight = new ScheduledFlight(
                OffsetDateTime.now(), OffsetDateTime.now().plusHours(1), FlightStatus.SCHEDULED, originCode, destinationCode, mockRegistrationNumber
        );
        try {
            Field departureField = ScheduledFlight.class.getDeclaredField("departureDateTime");
            departureField.setAccessible(true);
            departureField.set(flight, null);

            Field arrivalField = ScheduledFlight.class.getDeclaredField("arrivalDateTime");
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
                new ScheduledFlight(null, arrival, FlightStatus.SCHEDULED, originCode, destinationCode, mockRegistrationNumber));
    }

    @Test
    void ensureConstructorThrowsExceptionForNullArrivalDateTime() {
        OffsetDateTime departure = OffsetDateTime.now();
        assertThrows(InvalidFlightScheduleException.class, () ->
                new ScheduledFlight(departure, null, FlightStatus.SCHEDULED, originCode, destinationCode, mockRegistrationNumber));
    }

    @Test
    void ensureConstructorThrowsExceptionForNullStatus() {
        OffsetDateTime departure = OffsetDateTime.now();
        OffsetDateTime arrival = departure.plusHours(2);
        assertThrows(InvalidFlightScheduleException.class, () ->
                new ScheduledFlight(departure, arrival, null, originCode, destinationCode, mockRegistrationNumber));
    }

    @Test
    void ensureConstructorThrowsExceptionForNullOriginCode() {
        OffsetDateTime departure = OffsetDateTime.now();
        OffsetDateTime arrival = departure.plusHours(2);
        assertThrows(InvalidFlightScheduleException.class, () ->
                new ScheduledFlight(departure, arrival, FlightStatus.SCHEDULED, null, destinationCode, mockRegistrationNumber));
    }

    @Test
    void ensureConstructorThrowsExceptionForNullAircraftRegistrationNumber() {
        OffsetDateTime departure = OffsetDateTime.now();
        OffsetDateTime arrival = departure.plusHours(2);
        assertThrows(InvalidFlightScheduleException.class, () ->
                new ScheduledFlight(departure, arrival, FlightStatus.SCHEDULED, originCode, destinationCode, null));
    }

    @Test
    void ensureConstructorThrowsExceptionWhenDepartureIsAfterArrival() {
        OffsetDateTime departure = OffsetDateTime.now().plusHours(2);
        OffsetDateTime arrival = OffsetDateTime.now();
        assertThrows(InvalidFlightScheduleException.class, () ->
                new ScheduledFlight(departure, arrival, FlightStatus.SCHEDULED, originCode, destinationCode, mockRegistrationNumber));
    }

    @Test
    void ensureConstructorThrowsExceptionWhenDepartureIsSameAsArrival() {
        OffsetDateTime departure = OffsetDateTime.now();
        OffsetDateTime arrival = departure;
        assertThrows(InvalidFlightScheduleException.class, () ->
                new ScheduledFlight(departure, arrival, FlightStatus.SCHEDULED, originCode, destinationCode, mockRegistrationNumber));
    }

    @Test
    void ensureGetDurationReturnsZeroIfDepartureDateIsNull() {
        ScheduledFlight flight = new ScheduledFlight(
                OffsetDateTime.now(), OffsetDateTime.now().plusHours(1), FlightStatus.SCHEDULED, originCode, destinationCode, mockRegistrationNumber
        );
        try {
            Field departureField = ScheduledFlight.class.getDeclaredField("departureDateTime");
            departureField.setAccessible(true);
            departureField.set(flight, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set fields to null via reflection: " + e.getMessage());
        }
        assertEquals(Duration.ZERO, flight.getDuration());
    }

    @Test
    void ensureGetDurationReturnsZeroIfArrivalDateIsNull() {
        ScheduledFlight flight = new ScheduledFlight(
                OffsetDateTime.now(), OffsetDateTime.now().plusHours(1), FlightStatus.SCHEDULED, originCode, destinationCode, mockRegistrationNumber
        );
        try {
            Field arrivalField = ScheduledFlight.class.getDeclaredField("arrivalDateTime");
            arrivalField.setAccessible(true);
            arrivalField.set(flight, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set fields to null via reflection: " + e.getMessage());
        }
        assertEquals(Duration.ZERO, flight.getDuration());
    }

    @Test
    void ensureConstructorThrowsExceptionForNullDestinationCode() {
        OffsetDateTime departure = OffsetDateTime.now();
        OffsetDateTime arrival = departure.plusHours(2);
        assertThrows(InvalidFlightScheduleException.class, () ->
                new ScheduledFlight(departure, arrival, FlightStatus.SCHEDULED, originCode, null, mockRegistrationNumber));
    }
}
