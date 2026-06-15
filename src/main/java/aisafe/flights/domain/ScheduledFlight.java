package aisafe.flights.domain;

import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.routes.domain.Route;
import aisafe.shared.domain.DomainException;
import java.time.OffsetDateTime;
import java.time.Duration;

/**
 * Domain model for a scheduled flight.
 */
public class ScheduledFlight {
    private Long id;
    private OffsetDateTime departureDateTime;
    private OffsetDateTime arrivalDateTime;
    private FlightStatus status;
    private Route route;
    private RegistrationNumber aircraftRegistrationNumber;

    public ScheduledFlight(OffsetDateTime departureDateTime, OffsetDateTime arrivalDateTime,
                           FlightStatus status, Route route, RegistrationNumber aircraftRegistrationNumber) {
        if (departureDateTime == null) {
            throw new InvalidFlightScheduleException("Departure date/time must not be null.");
        }
        if (arrivalDateTime == null) {
            throw new InvalidFlightScheduleException("Arrival date/time must not be null.");
        }
        if (status == null) {
            throw new InvalidFlightScheduleException("Flight status must not be null.");
        }
        if (route == null) {
            throw new InvalidFlightScheduleException("Route must not be null.");
        }
        if (aircraftRegistrationNumber == null) {
            throw new InvalidFlightScheduleException("Aircraft registration number must not be null.");
        }
        if (!departureDateTime.isBefore(arrivalDateTime)) {
            throw new InvalidFlightScheduleException("Departure must be before arrival.");
        }
        this.departureDateTime = departureDateTime;
        this.arrivalDateTime = arrivalDateTime;
        this.status = status;
        this.route = route;
        this.aircraftRegistrationNumber = aircraftRegistrationNumber;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public OffsetDateTime getDepartureDateTime() { return departureDateTime; }
    public OffsetDateTime getArrivalDateTime() { return arrivalDateTime; }
    public FlightStatus getStatus() { return status; }
    public Route getRoute() { return route; }
    public RegistrationNumber getAircraftRegistrationNumber() { return aircraftRegistrationNumber; }

    public Duration getDuration() {
        if (departureDateTime == null || arrivalDateTime == null) return Duration.ZERO;
        return Duration.between(departureDateTime, arrivalDateTime);
    }
}
