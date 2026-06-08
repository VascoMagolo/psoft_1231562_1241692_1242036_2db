package aisafe.flights.infrastructure.persistence;

import aisafe.flights.domain.Flight;
import aisafe.flights.domain.FlightRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
@Profile("jpa")
public class FlightJpaRepository implements FlightRepository {

    private final SpringDataFlightRepository springRepo;

    public FlightJpaRepository(SpringDataFlightRepository springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public Flight save(Flight flight) {
        FlightJpaEntity saved = springRepo.save(FlightMapper.toJpa(flight));
        flight.setId(saved.getId());
        return flight;
    }

    @Override
    public List<Flight> findByAircraftRegistrationNumber(String aircraftRegistrationNumber) {
        return springRepo.findByAircraftRegistrationNumberOrderByDepartureDateTimeAsc(aircraftRegistrationNumber)
                .stream()
                .map(FlightMapper::toDomain)
                .toList();
    }

    @Override
    public boolean hasOverlappingFlights(String aircraftRegistrationNumber, OffsetDateTime departureDateTime, OffsetDateTime arrivalDateTime) {
        return springRepo.hasOverlappingFlights(aircraftRegistrationNumber, departureDateTime, arrivalDateTime);
    }

    @Override
    public long countByRouteId(Long routeId) {
        return springRepo.countByRouteId(routeId);
    }
}
