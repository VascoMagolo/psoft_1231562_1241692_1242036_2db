package aisafe.flights.application.dtos;

import aisafe.flights.domain.Flight;

import java.time.OffsetDateTime;

public record FlightResponse(
        Long id,
        String aircraftId,
        Long routeId,
        OffsetDateTime departureDateTime,
        OffsetDateTime arrivalDateTime
) {
    public static FlightResponse from(Flight flight) {
        return new FlightResponse(
                flight.getId(),
                flight.getAircraftRegistrationNumber(),
                flight.getRouteId(),
                flight.getDepartureDateTime(),
                flight.getArrivalDateTime()
        );
    }
}
