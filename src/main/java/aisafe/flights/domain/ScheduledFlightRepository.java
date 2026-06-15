package aisafe.routes.domain;

import aisafe.airports.domain.IataCode;
import aisafe.shared.domain.BaseRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduledFlightRepository extends BaseRepository<ScheduledFlight> {
    Optional<ScheduledFlight> findById(Long id);
    List<ScheduledFlight> findByAircraftRegistration(String registration);
    List<ScheduledFlight> findFlightsForUtilization(String registration, OffsetDateTime start, OffsetDateTime end);
    boolean hasOverlappingFlights(String registration, OffsetDateTime departureDateTime, OffsetDateTime arrivalDateTime);
    boolean existsByAircraftRegistration(String registration);
    long countByRoute(IataCode origin, IataCode destination);
}
