package aisafe.aircrafts.infrastructure.persistence.jpa;

import aisafe.aircrafts.infrastructure.readmodel.AircraftProjection;
import aisafe.shared.infrastructure.messaging.EventPublisher;
import aisafe.shared.infrastructure.replication.Replicator;
import aisafe.aircrafts.infrastructure.replication.AircraftReplicaHandler;
import aisafe.aircrafts.infrastructure.replication.AircraftReplica;
import aisafe.aircrafts.domain.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Profile("jpa")
public class AircraftJpaRepository implements AircraftRepository {

    private final SpringDataAircraftRepository springRepo;
    private final SpringDataAircraftModelRepository modelSpringRepo;
    private final Replicator replicator;
    private final EventPublisher events;

    public AircraftJpaRepository(SpringDataAircraftRepository springRepo, SpringDataAircraftModelRepository modelSpringRepo,
                                 Replicator replicator, EventPublisher events) {
        this.springRepo = springRepo;
        this.modelSpringRepo = modelSpringRepo;
        this.replicator = replicator;
        this.events = events;
    }

    @Override
    public long count() {
        return springRepo.count();
    }

    @Override
    public Optional<Aircraft> findByRegistrationNumber(RegistrationNumber number) {
        return springRepo.findByRegistrationNumber(new RegistrationNumberJpaEmbeddable(number.getNumber()))
                .map(jpa -> AircraftMapper.toDomain(jpa, AircraftModelMapper.toDomain(jpa.getModel())));
    }

    @Override
    public boolean existsByRegistrationNumber(RegistrationNumber number) {
        return springRepo.existsByRegistrationNumber(new RegistrationNumberJpaEmbeddable(number.getNumber()));
    }

    @Override
    public List<Aircraft> findAll() {
        return springRepo.findAll().stream()
                .map(jpa -> AircraftMapper.toDomain(jpa, AircraftModelMapper.toDomain(jpa.getModel())))
                .collect(Collectors.toList());
    }

    @Override
    public Aircraft save(Aircraft aircraft) {
        AircraftJpaEntity existingEntity = springRepo.findByRegistrationNumber(
                new RegistrationNumberJpaEmbeddable(aircraft.getRegistrationNumber().getNumber()))
                .orElse(null);

        AircraftModelJpaEntity managedModel = modelSpringRepo.findByModelName(aircraft.getModel().getModelName())
                .orElseThrow(() -> new AircraftModelNotFoundException(
                        "Aircraft model not found in DB: " + aircraft.getModel().getModelName()));

        AircraftJpaEntity newJpaData = AircraftMapper.toJpa(aircraft, managedModel);

        if (existingEntity != null) {
            newJpaData.setId(existingEntity.getId());
            newJpaData.setVersion(existingEntity.getVersion());
        }

        AircraftJpaEntity saved = springRepo.saveAndFlush(newJpaData);
        // Record: copied to the other holder (home replica and its successor)
        replicator.recordSaved(AircraftReplicaHandler.TYPE, aircraft.getRegistrationNumber().getNumber(),
                AircraftReplica.from(aircraft), saved.getVersion());
        events.changed(AircraftReplicaHandler.TYPE, AircraftProjection.ENTITY, existingEntity != null ? "updated" : "created",
                aircraft.getRegistrationNumber().getNumber(), aircraft.getStatus(), saved.getVersion(), AircraftReplica.from(aircraft));
        return aircraft;
    }

    @Override
    public Long findVersionFor(RegistrationNumber reg) {
        return springRepo.findByRegistrationNumber(new RegistrationNumberJpaEmbeddable(reg.getNumber()))
                .map(AircraftJpaEntity::getVersion)
                .orElse(0L);
    }

    @Override
    public void delete(Aircraft aircraft) {
        AircraftJpaEntity jpaEntity = springRepo.findByRegistrationNumber(new RegistrationNumberJpaEmbeddable(aircraft.getRegistrationNumber().getNumber()))
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft not found: " + aircraft.getRegistrationNumber().getNumber()));
        springRepo.delete(jpaEntity);
        replicator.recordDeleted(AircraftReplicaHandler.TYPE, aircraft.getRegistrationNumber().getNumber());
        events.changed(AircraftReplicaHandler.TYPE, AircraftProjection.ENTITY, "deleted",
                aircraft.getRegistrationNumber().getNumber(), null, null, null);
    }
}
