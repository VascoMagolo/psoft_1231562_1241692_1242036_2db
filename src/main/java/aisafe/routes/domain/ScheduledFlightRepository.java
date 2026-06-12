package aisafe.routes.domain;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.List;

public interface ScheduledFlightRepository {
    ScheduledFlight save(ScheduledFlight flight);
    Optional<ScheduledFlight> findById(Long id);
    List<ScheduledFlight> findAll();
    long count();
    List<ScheduledFlight> findFlightsForUtilization(String registration, OffsetDateTime start, OffsetDateTime end);
    boolean existsByAircraftRegistration(String registration);
}
