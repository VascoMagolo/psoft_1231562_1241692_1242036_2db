package aisafe.flights.domain;

import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.routes.domain.Route;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ScheduledFlightTest {

    @Test
    void ensureScheduledFlightIsCreatedWithValidArguments() {
        OffsetDateTime departure = OffsetDateTime.now().plusDays(1);
        OffsetDateTime arrival = departure.plusHours(2);
        Route route = new Route("OPO", "LIS", 60, 100.0, 100);
        RegistrationNumber registrationNumber = new RegistrationNumber("CS-ABC");

        assertDoesNotThrow(() -> new ScheduledFlight(departure, arrival, FlightStatus.SCHEDULED, route, registrationNumber));
    }

    @Test
    void ensureScheduledFlightThrowsExceptionWhenDepartureIsAfterArrival() {
        OffsetDateTime departure = OffsetDateTime.now().plusDays(1);
        OffsetDateTime arrival = departure.minusHours(1);
        Route route = new Route("OPO", "LIS", 60, 100.0, 100);
        RegistrationNumber registrationNumber = new RegistrationNumber("CS-ABC");

        assertThrows(InvalidFlightScheduleException.class, () -> new ScheduledFlight(departure, arrival, FlightStatus.SCHEDULED, route, registrationNumber));
    }
}
