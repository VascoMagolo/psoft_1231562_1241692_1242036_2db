package aisafe.flights.domain;

import java.time.OffsetDateTime;
import java.util.List;

public interface FlightRepository {
    Flight save(Flight flight);
    List<Flight> findByAircraftRegistrationNumber(String aircraftRegistrationNumber);
    boolean hasOverlappingFlights(String aircraftRegistrationNumber, OffsetDateTime departureDateTime, OffsetDateTime arrivalDateTime);
    long countByRouteId(Long routeId);
}
