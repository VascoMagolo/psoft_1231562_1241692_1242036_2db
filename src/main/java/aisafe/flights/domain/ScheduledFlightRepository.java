package aisafe.flights.domain;

import aisafe.airports.domain.IataCode;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.shared.domain.BaseRepository;

import aisafe.shared.domain.PaginatedResult;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduledFlightRepository extends BaseRepository<ScheduledFlight> {
    Optional<ScheduledFlight> findById(Long id);
    List<ScheduledFlight> findByAircraftRegistration(RegistrationNumber registration);
    List<ScheduledFlight> findFlightsForUtilization(RegistrationNumber registration, OffsetDateTime start, OffsetDateTime end);
    boolean existsByOverlappingSchedule(RegistrationNumber registration, OffsetDateTime departureDateTime, OffsetDateTime arrivalDateTime);
    boolean existsByAircraftRegistration(RegistrationNumber registration);
    long countByRoute(IataCode origin, IataCode destination);
    List<ModelUtilizationData> findTopModelsByFlightHours(int limit);
    List<ModelUtilizationData> findTopModelsByAssignments(int limit);
    Double calculateTotalOperationalHoursByRegistration(RegistrationNumber registration);
    PaginatedResult<RouteUtilizationData> getFlightUtilizationReport(OffsetDateTime startDate, OffsetDateTime endDate, int page, int size);
    Double calculateOperationalHoursSince(RegistrationNumber registration, OffsetDateTime sinceDate);
}
