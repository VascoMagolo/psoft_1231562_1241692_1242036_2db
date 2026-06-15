package aisafe.flights.application.dtos;

import aisafe.flights.domain.FlightStatus;
import aisafe.flights.domain.ScheduledFlight;

import java.time.OffsetDateTime;

public record FlightResponse(
        Long id,
        String aircraftId,
        String originIataCode,
        String destinationIataCode,
        OffsetDateTime departureDateTime,
        OffsetDateTime arrivalDateTime,
        FlightStatus status
) {
    public static FlightResponse from(ScheduledFlight flight) {
        return new FlightResponse(
                flight.getId(),
                flight.getAircraftRegistrationNumber().getNumber(),
                flight.getRoute().getOrigin().getCode(),
                flight.getRoute().getDestination().getCode(),
                flight.getDepartureDateTime(),
                flight.getArrivalDateTime(),
                flight.getStatus()
        );
    }
}
