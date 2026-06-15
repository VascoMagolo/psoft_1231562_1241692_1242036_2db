package aisafe.flights.domain;

import aisafe.aircrafts.domain.Aircraft;
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
    private Aircraft aircraft;

    public ScheduledFlight(OffsetDateTime departureDateTime, OffsetDateTime arrivalDateTime,
                           FlightStatus status, Route route, Aircraft aircraft) {
        if (departureDateTime == null) {
            throw new DomainException("Departure date/time must not be null.");
        }
        if (arrivalDateTime == null) {
            throw new DomainException("Arrival date/time must not be null.");
        }
        if (status == null) {
            throw new DomainException("Flight status must not be null.");
        }
        if (route == null) {
            throw new DomainException("Route must not be null.");
        }
        if (aircraft == null) {
            throw new DomainException("Aircraft must not be null.");
        }
        if (!departureDateTime.isBefore(arrivalDateTime)) {
            throw new DomainException("Departure must be before arrival.");
        }
        this.departureDateTime = departureDateTime;
        this.arrivalDateTime = arrivalDateTime;
        this.status = status;
        this.route = route;
        this.aircraft = aircraft;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public OffsetDateTime getDepartureDateTime() { return departureDateTime; }
    public OffsetDateTime getArrivalDateTime() { return arrivalDateTime; }
    public FlightStatus getStatus() { return status; }
    public Route getRoute() { return route; }
    public Aircraft getAircraft() { return aircraft; }

    public Duration getDuration() {
        if (departureDateTime == null || arrivalDateTime == null) return Duration.ZERO;
        return Duration.between(departureDateTime, arrivalDateTime);
    }
}
