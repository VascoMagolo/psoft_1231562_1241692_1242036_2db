package aisafe.flights.domain;

import aisafe.shared.domain.DomainException;

import java.time.OffsetDateTime;

public class Flight {

    private Long id;
    private final String aircraftRegistrationNumber;
    private final Long routeId;
    private final OffsetDateTime departureDateTime;
    private final OffsetDateTime arrivalDateTime;

    public Flight(String aircraftRegistrationNumber, Long routeId,
                  OffsetDateTime departureDateTime, OffsetDateTime arrivalDateTime) {
        if (aircraftRegistrationNumber == null || aircraftRegistrationNumber.isBlank()) {
            throw new DomainException("Aircraft registration number cannot be blank");
        }
        if (routeId == null) {
            throw new DomainException("Route ID must not be null");
        }
        if (departureDateTime == null) {
            throw new DomainException("Departure datetime must not be null");
        }
        if (arrivalDateTime == null) {
            throw new DomainException("Arrival datetime must not be null");
        }
        if (!arrivalDateTime.isAfter(departureDateTime)) {
            throw new DomainException("Arrival datetime must be after departure datetime");
        }

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
