package aisafe.routes.infrastructure.persistence;

import aisafe.aircrafts.infrastructure.persistence.AircraftJpaEntity;
import aisafe.aircrafts.infrastructure.persistence.SpringDataAircraftRepository;
import aisafe.routes.domain.ScheduledFlight;
import aisafe.routes.domain.ScheduledFlightRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import aisafe.aircrafts.infrastructure.persistence.RegistrationNumberJpaEmbeddable;

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
    public ScheduledFlight save(ScheduledFlight flight) {
        AircraftJpaEntity aircraftEntity = aircraftRepo.findByRegistrationNumber(new RegistrationNumberJpaEmbeddable(flight.getAircraft().getRegistrationNumber().getNumber()))
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found for flight"));

        // Find route logic using origin and destination since we don't have a direct route ID in the domain model easily accessible here without fetching
        RouteJpaEntity routeEntity = routeRepo.findByOriginCodeOrDestinationCode(flight.getRoute().getOrigin().getCode(), flight.getRoute().getDestination().getCode())
                .stream()
                .filter(r -> r.getOriginCode().equals(flight.getRoute().getOrigin().getCode()) && r.getDestinationCode().equals(flight.getRoute().getDestination().getCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Route not found for flight"));

        ScheduledFlightJpaEntity entity = new ScheduledFlightJpaEntity(
                flight.getDepartureDateTime(),
                flight.getArrivalDateTime(),
                flight.getStatus(),
                routeEntity,
                aircraftEntity
        );
        entity.setId(flight.getId());

        ScheduledFlightJpaEntity saved = springRepo.save(entity);
        return ScheduledFlightMapper.toDomain(saved);
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
    public boolean existsByAircraftRegistration(String registration) {
        return springRepo.existsByAircraftRegistrationNumberNumber(registration);
    }
}
