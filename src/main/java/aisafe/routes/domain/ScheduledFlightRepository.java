package aisafe.routes.domain;

import aisafe.shared.domain.BaseRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduledFlightRepository extends BaseRepository<ScheduledFlight> {
    Optional<ScheduledFlight> findById(Long id);
    List<ScheduledFlight> findFlightsForUtilization(String registration, OffsetDateTime start, OffsetDateTime end);
    boolean existsByAircraftRegistration(String registration);
}
