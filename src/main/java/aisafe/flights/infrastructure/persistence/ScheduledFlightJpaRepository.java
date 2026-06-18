package aisafe.flights.infrastructure.persistence;

import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.airports.domain.IataCode;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.aircrafts.infrastructure.persistence.jpa.AircraftJpaEntity;
import aisafe.aircrafts.infrastructure.persistence.jpa.SpringDataAircraftRepository;
import aisafe.flights.domain.RouteUtilizationData;
import aisafe.routes.domain.RouteNotFoundException;
import aisafe.flights.domain.ModelUtilizationData;
import aisafe.flights.domain.ScheduledFlight;
import aisafe.flights.domain.ScheduledFlightRepository;
import aisafe.routes.infrastructure.persistence.jpa.RouteJpaEntity;
import aisafe.routes.infrastructure.persistence.jpa.SpringDataRouteRepository;
import aisafe.shared.domain.PaginatedResult;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import aisafe.aircrafts.infrastructure.persistence.jpa.RegistrationNumberJpaEmbeddable;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Repository
@Profile("jpa")
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
    public List<ModelUtilizationData> findTopModelsByFlightHours(int limit) {
        return springRepo.findTopModelsByFlightHours(PageRequest.of(0, limit)).stream()
                .map(p -> new ModelUtilizationData(p.getModelName(), p.getUtilizationValue()))
                .toList();
    }

    @Override
    public List<ModelUtilizationData> findTopModelsByAssignments(int limit) {
        return springRepo.findTopModelsByAssignments(PageRequest.of(0, limit)).stream()
                .map(p -> new ModelUtilizationData(p.getModelName(), p.getUtilizationValue()))
                .toList();
    }

    @Override
    public void save(ScheduledFlight flight) {
        AircraftJpaEntity aircraftEntity = aircraftRepo.findByRegistrationNumber(new RegistrationNumberJpaEmbeddable(flight.getAircraftRegistrationNumber().getNumber()))
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
    public List<ScheduledFlight> findByAircraftRegistration(RegistrationNumber registration) {
        return springRepo.findByAircraftRegistration(registration.getNumber()).stream()
                .map(ScheduledFlightMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return springRepo.count();
    }

    @Override
    public List<ScheduledFlight> findFlightsForUtilization(RegistrationNumber registration, OffsetDateTime start, OffsetDateTime end) {
        return springRepo.findFlightsForUtilization(registration.getNumber(), start, end).stream()
                .map(ScheduledFlightMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasOverlappingFlights(RegistrationNumber registration, OffsetDateTime departureDateTime, OffsetDateTime arrivalDateTime) {
        return springRepo.hasOverlappingFlights(registration.getNumber(), departureDateTime, arrivalDateTime);
    }

    @Override
    public boolean existsByAircraftRegistration(RegistrationNumber registration) {
        return springRepo.existsByAircraftRegistrationNumberNumber(registration.getNumber());
    }

    @Override
    public long countByRoute(IataCode origin, IataCode destination) {
        return springRepo.countByRoute(origin.getCode(), destination.getCode());
    }

    @Override
    public Double calculateTotalOperationalHoursByRegistration(RegistrationNumber registration) {
        return springRepo.calculateTotalOperationalHoursByRegistration(registration.getNumber());
    }

    @Override
    public Double calculateOperationalHoursSince(RegistrationNumber registration, OffsetDateTime sinceDate) {
        return springRepo.calculateOperationalHoursSince(registration.getNumber(), sinceDate);
    }

    @Override
    public PaginatedResult<RouteUtilizationData> getFlightUtilizationReport(OffsetDateTime startDate, OffsetDateTime endDate, int page, int size) {
        Page<RouteUtilizationProjection> resultPage = springRepo.findFlightUtilizationReports(startDate, endDate, PageRequest.of(page, size));
        List<RouteUtilizationData> data = resultPage.stream()
                .map(p -> new RouteUtilizationData(
                        p.getRouteId(),
                        p.getOriginCode(),
                        p.getDestinationCode(),
                        p.getFlightCount()
                ))
                .collect(Collectors.toList());
        return new PaginatedResult<>(data, resultPage.getTotalElements());
    }
}
