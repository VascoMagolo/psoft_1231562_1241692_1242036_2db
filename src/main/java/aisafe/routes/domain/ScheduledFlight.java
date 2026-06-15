package aisafe.routes.domain;

import aisafe.aircrafts.domain.Aircraft;
import org.springframework.util.Assert;
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
        Assert.notNull(departureDateTime, "Departure date/time must not be null.");
        Assert.notNull(arrivalDateTime, "Arrival date/time must not be null.");
        Assert.notNull(status, "Flight status must not be null.");
        Assert.notNull(route, "Route must not be null.");
        Assert.notNull(aircraft, "Aircraft must not be null.");
        Assert.isTrue(departureDateTime.isBefore(arrivalDateTime), "Departure must be before arrival.");
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
