package aisafe.aircrafts.infrastructure.persistence.jpa;

import aisafe.aircrafts.infrastructure.readmodel.AircraftModelProjection;
import aisafe.aircrafts.infrastructure.readmodel.AircraftModelView;
import aisafe.shared.infrastructure.messaging.EventPublisher;
import aisafe.shared.infrastructure.replication.Replicator;
import aisafe.aircrafts.infrastructure.replication.AircraftModelReplicaHandler;
import aisafe.aircrafts.infrastructure.replication.AircraftModelReplica;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelRepository;
import aisafe.shared.domain.PaginatedResult;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Profile("jpa")
public class AircraftModelJpaRepository implements AircraftModelRepository {

    private final SpringDataAircraftModelRepository springRepo;
    private final Replicator replicator;
    private final EventPublisher events;

    public AircraftModelJpaRepository(SpringDataAircraftModelRepository springRepo, Replicator replicator,
                                      EventPublisher events) {
        this.springRepo = springRepo;
        this.replicator = replicator;
        this.events = events;
    }

    @Override
    public long count() {
        return springRepo.count();
    }

    @Override
    public Optional<AircraftModel> findByModelName(String modelName) {
        return springRepo.findByModelName(modelName)
                .map(AircraftModelMapper::toDomain);
    }

    @Override
    public boolean existsByModelName(String modelName) {
        return springRepo.existsByModelName(modelName);
    }

    @Override
    public List<AircraftModel> findAll() {
        return springRepo.findAll().stream()
                .map(AircraftModelMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatedResult<AircraftModel> findAll(int pageNumber, int pageSize) {
        var springPageable = PageRequest.of(pageNumber, pageSize);
        var page = springRepo.findAll(springPageable);

        List<AircraftModel> list = page.stream()
                .map(AircraftModelMapper::toDomain)
                .collect(Collectors.toList());

        return new PaginatedResult<>(list, page.getTotalElements());
    }

    @Override
    public AircraftModel save(AircraftModel domainModel) {
        AircraftModelJpaEntity jpaEntity = springRepo.findByModelName(domainModel.getModelName())
                .orElse(new AircraftModelJpaEntity());

        AircraftModelJpaEntity newData = AircraftModelMapper.toJpa(domainModel);

        if (jpaEntity.getId() != null) {
            newData.setId(jpaEntity.getId());
        }

        springRepo.save(newData);
        // Catalog: copied to every replica (decision R1)
        replicator.catalogSaved(AircraftModelReplicaHandler.TYPE, domainModel.getModelName(), AircraftModelReplica.from(domainModel));
        events.changed(AircraftModelReplicaHandler.TYPE, AircraftModelProjection.ENTITY,
                jpaEntity.getId() != null ? "updated" : "created", domainModel.getModelName(), null, null,
                AircraftModelView.from(domainModel));
        return domainModel;
    }

    @Override
    public void delete(AircraftModel domainModel) {
        AircraftModelJpaEntity jpaEntity = springRepo.findByModelName(domainModel.getModelName())
                .orElseThrow(() -> new IllegalArgumentException("Aircraft model not found: " + domainModel.getModelName()));
        springRepo.delete(jpaEntity);
        replicator.catalogDeleted(AircraftModelReplicaHandler.TYPE, domainModel.getModelName());
        events.changed(AircraftModelReplicaHandler.TYPE, AircraftModelProjection.ENTITY, "deleted",
                domainModel.getModelName(), null, null, null);
    }
}
