package aisafe.flights.infrastructure.persistence.jpa;

import aisafe.flights.domain.RegistrationNumber;
import aisafe.flights.domain.ScheduledFlight;
import aisafe.flights.domain.ScheduledFlightRepository;
import aisafe.flights.infrastructure.readmodel.FlightProjection;
import aisafe.flights.infrastructure.replication.FlightReplica;
import aisafe.flights.infrastructure.replication.FlightReplicaHandler;
import aisafe.shared.infrastructure.messaging.EventPublisher;
import aisafe.shared.infrastructure.replication.Replicator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Profile("jpa")
public class ScheduledFlightJpaRepository implements ScheduledFlightRepository {

    private final SpringDataScheduledFlightRepository springRepo;
    private final Replicator replicator;
    private final EventPublisher events;

    // A flight's id must be the same on every replica that holds it and never given twice, without
    // the replicas agreeing on it: each replica gives the ids that end in its own digit
    // (flight-ops-0: 1, 11, 21...; flight-ops-1: 2, 12, 22...), after the highest one it holds. A
    // replica reborn empty recovers its flights from its peers before it is ready, so it goes on
    // after them. Up to 10 replicas.
    private static final long ID_STEP = 10;
    private final long idRest;
    private Long lastId;

    public ScheduledFlightJpaRepository(SpringDataScheduledFlightRepository springRepo,
                                        Replicator replicator,
                                        EventPublisher events,
                                        @Value("${aisafe.node.id}") String nodeId) {
        this.springRepo = springRepo;
        this.replicator = replicator;
        this.events = events;
        this.idRest = (replicaIndex(nodeId) + 1) % ID_STEP;
    }

    /** flight-ops-2 -> 2; a name without a number (local development) -> 0. */
    private static int replicaIndex(String nodeId) {
        String digits = nodeId.substring(nodeId.lastIndexOf('-') + 1);
        int index = digits.matches("\\d+") ? Integer.parseInt(digits) : 0;
        if (index >= ID_STEP) {
            throw new IllegalStateException("Flight ids support up to " + ID_STEP + " replicas, not " + nodeId);
        }
        return index;
    }

    private synchronized long nextFlightId() {
        if (lastId == null) {
            Long highest = springRepo.findMaxFlightId(ID_STEP, idRest);
            lastId = highest != null ? highest : (idRest == 0 ? 0 : idRest - ID_STEP);
        }
        lastId += ID_STEP;
        return lastId;
    }

    @Override
    public long count() {
        return springRepo.count();
    }

    @Override
    public ScheduledFlight save(ScheduledFlight flight) {
        Optional<ScheduledFlightJpaEntity> existing = Optional.empty();
        if (flight.getId() != null) {
            existing = springRepo.findByFlightId(flight.getId());
        }
        if (existing.isEmpty()) {
            existing = springRepo.findByAircraftRegistrationAndDepartureDateTime(
                    flight.getAircraftRegistrationNumber().getNumber(),
                    flight.getDepartureDateTime());
        }

        if (existing.isPresent()) {
            ScheduledFlightJpaEntity entity = existing.get();
            entity.setDepartureDateTime(flight.getDepartureDateTime());
            entity.setArrivalDateTime(flight.getArrivalDateTime());
            entity.setStatus(flight.getStatus());
            entity.setOriginIataCode(flight.getOriginCode().getCode());
            entity.setDestinationIataCode(flight.getDestinationCode().getCode());
            entity.setAircraftRegistration(flight.getAircraftRegistrationNumber().getNumber());
            if (flight.getVersion() != null) {
                entity.setVersion(flight.getVersion());
            }
            ScheduledFlightJpaEntity saved = springRepo.saveAndFlush(entity);
            ScheduledFlight result = ScheduledFlightMapper.toDomain(saved);
            replicator.recordSaved(FlightReplicaHandler.TYPE, String.valueOf(saved.getFlightId()), FlightReplica.from(result), saved.getVersion());
            events.changed(FlightReplicaHandler.TYPE, FlightProjection.ENTITY, "updated", String.valueOf(saved.getFlightId()),
                    result.getStatus(), saved.getVersion(), FlightReplica.from(result));
            return result;
        } else {
            // A new flight gets its id here; a flight from a peer keeps the one it has
            ScheduledFlightJpaEntity entity = new ScheduledFlightJpaEntity(
                    flight.getId() != null ? flight.getId() : nextFlightId(),
                    flight.getDepartureDateTime(),
                    flight.getArrivalDateTime(),
                    flight.getStatus(),
                    flight.getOriginCode().getCode(),
                    flight.getDestinationCode().getCode(),
                    flight.getAircraftRegistrationNumber().getNumber()
            );
            ScheduledFlightJpaEntity saved = springRepo.saveAndFlush(entity);
            ScheduledFlight result = ScheduledFlightMapper.toDomain(saved);
            replicator.recordSaved(FlightReplicaHandler.TYPE, String.valueOf(saved.getFlightId()), FlightReplica.from(result), saved.getVersion());
            events.changed(FlightReplicaHandler.TYPE, FlightProjection.ENTITY, "created", String.valueOf(saved.getFlightId()),
                    result.getStatus(), saved.getVersion(), FlightReplica.from(result));
            return result;
        }
    }

    @Override
    public void delete(ScheduledFlight flight) {
        Optional<ScheduledFlightJpaEntity> entity = flight.getId() != null
                ? springRepo.findByFlightId(flight.getId())
                : springRepo.findByAircraftRegistrationAndDepartureDateTime(
                        flight.getAircraftRegistrationNumber().getNumber(),
                        flight.getDepartureDateTime());
        entity.ifPresent(e -> {
            springRepo.delete(e);
            replicator.recordDeleted(FlightReplicaHandler.TYPE, String.valueOf(e.getFlightId()));
            events.changed(FlightReplicaHandler.TYPE, FlightProjection.ENTITY, "deleted", String.valueOf(e.getFlightId()), null, null, null);
        });
    }

    @Override
    public Optional<ScheduledFlight> findById(Long id) {
        return springRepo.findByFlightId(id).map(ScheduledFlightMapper::toDomain);
    }

    @Override
    public Optional<ScheduledFlight> findByAircraftRegistrationAndDepartureDateTime(RegistrationNumber registration, OffsetDateTime departureDateTime) {
        return springRepo.findByAircraftRegistrationAndDepartureDateTime(registration.getNumber(), departureDateTime)
                .map(ScheduledFlightMapper::toDomain);
    }

    @Override
    public List<ScheduledFlight> findAll() {
        return springRepo.findAll().stream()
                .map(ScheduledFlightMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Long findVersionFor(Long id) {
        return springRepo.findVersionFor(id);
    }
}
