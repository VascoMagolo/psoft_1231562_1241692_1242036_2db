package aisafe.flights.domain;

import org.springframework.util.Assert;

import java.time.OffsetDateTime;

public class Flight {

    private Long id;
    private final String aircraftRegistrationNumber;
    private final Long routeId;
    private final OffsetDateTime departureDateTime;
    private final OffsetDateTime arrivalDateTime;

    public Flight(String aircraftRegistrationNumber, Long routeId,
                  OffsetDateTime departureDateTime, OffsetDateTime arrivalDateTime) {
        Assert.hasText(aircraftRegistrationNumber, "Aircraft registration number cannot be blank");
        Assert.notNull(routeId, "Route ID must not be null");
        Assert.notNull(departureDateTime, "Departure datetime must not be null");
        Assert.notNull(arrivalDateTime, "Arrival datetime must not be null");
        Assert.isTrue(arrivalDateTime.isAfter(departureDateTime), "Arrival datetime must be after departure datetime");

        this.aircraftRegistrationNumber = aircraftRegistrationNumber.trim().toUpperCase();
        this.routeId = routeId;
        this.departureDateTime = departureDateTime;
        this.arrivalDateTime = arrivalDateTime;
    }

    public Long getId() { return id; }
    public String getAircraftRegistrationNumber() { return aircraftRegistrationNumber; }
    public Long getRouteId() { return routeId; }
    public OffsetDateTime getDepartureDateTime() { return departureDateTime; }
    public OffsetDateTime getArrivalDateTime() { return arrivalDateTime; }

    public void setId(Long id) { this.id = id; }
}
