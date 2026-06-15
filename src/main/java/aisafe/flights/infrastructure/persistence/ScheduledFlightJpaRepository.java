package aisafe.routes.infrastructure.persistence.jpa;

import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.infrastructure.persistence.jpa.AircraftJpaEntity;
import aisafe.aircrafts.infrastructure.persistence.jpa.SpringDataAircraftRepository;
import aisafe.routes.domain.RouteNotFoundException;
import aisafe.routes.domain.ScheduledFlight;
import aisafe.routes.domain.ScheduledFlightRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import aisafe.aircrafts.infrastructure.persistence.jpa.RegistrationNumberJpaEmbeddable;

@Repository
public class ScheduledFlightJpaRepository implements ScheduledFlightRepository {

    private final SpringDataScheduledFlightRepository springRepo;
    private final SpringDataAircraftRepository aircraftRepo;
    private final SpringDataRouteRepository routeRepo;

    public ScheduledFlightJpaRepository(SpringDataScheduledFlightRepository springRepo,
                                        SpringDataAircraftRepository aircraftRepo,
                                        SpringDataRouteRepository routeRepo) {
        this.springRepo = springRepo;
        this.aircraftRepo = aircraftRepo;
        this.routeRepo = routeRepo;
    }

    @Override
    public void save(ScheduledFlight flight) {
        AircraftJpaEntity aircraftEntity = aircraftRepo.findByRegistrationNumber(new RegistrationNumberJpaEmbeddable(flight.getAircraft().getRegistrationNumber().getNumber()))
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft not found for scheduled flight"));

        RouteJpaEntity routeEntity = routeRepo.findByOriginCodeOrDestinationCode(flight.getRoute().getOrigin().getCode(), flight.getRoute().getDestination().getCode())
                .stream()
                .filter(r -> r.getOriginCode().equals(flight.getRoute().getOrigin().getCode()) && r.getDestinationCode().equals(flight.getRoute().getDestination().getCode()))
                .findFirst()
                .orElseThrow(() -> new RouteNotFoundException("Route not found for scheduled flight"));

        ScheduledFlightJpaEntity entity = new ScheduledFlightJpaEntity(
                flight.getDepartureDateTime(),
                flight.getArrivalDateTime(),
                flight.getStatus(),
                routeEntity,
                aircraftEntity
        );
        entity.setId(flight.getId());

        ScheduledFlightJpaEntity saved = springRepo.save(entity);
        flight.setId(saved.getId());
    }

    @Override
    public void delete(ScheduledFlight flight) {
        if (flight.getId() != null) {
            springRepo.deleteById(flight.getId());
        }
    }

    @Override
    public Optional<ScheduledFlight> findById(Long id) {
        return springRepo.findById(id).map(ScheduledFlightMapper::toDomain);
    }

    @Override
    public List<ScheduledFlight> findAll() {
        return springRepo.findAll().stream()
                .map(ScheduledFlightMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduledFlight> findByAircraftRegistration(String registration) {
        return springRepo.findByAircraftRegistration(registration).stream()
                .map(ScheduledFlightMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return springRepo.count();
    }

    @Override
    public List<ScheduledFlight> findFlightsForUtilization(String registration, OffsetDateTime start, OffsetDateTime end) {
        return springRepo.findFlightsForUtilization(registration, start, end).stream()
                .map(ScheduledFlightMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasOverlappingFlights(String registration, OffsetDateTime departureDateTime, OffsetDateTime arrivalDateTime) {
        return springRepo.hasOverlappingFlights(registration, departureDateTime, arrivalDateTime);
    }

    @Override
    public boolean existsByAircraftRegistration(String registration) {
        return springRepo.existsByAircraftRegistrationNumberNumber(registration);
    }

    @Override
    public long countByRoute(aisafe.airports.domain.IataCode origin, aisafe.airports.domain.IataCode destination) {
        return springRepo.countByRoute(origin.getCode(), destination.getCode());
    }
}
