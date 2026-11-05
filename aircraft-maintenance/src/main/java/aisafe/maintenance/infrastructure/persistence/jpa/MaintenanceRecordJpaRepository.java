package aisafe.maintenance.infrastructure.persistence.jpa;

import aisafe.maintenance.infrastructure.readmodel.MaintenanceRecordProjection;
import aisafe.shared.infrastructure.messaging.EventPublisher;
import aisafe.shared.infrastructure.replication.Replicator;
import aisafe.maintenance.infrastructure.replication.MaintenanceRecordReplicaHandler;
import aisafe.maintenance.infrastructure.replication.MaintenanceRecordReplica;
import aisafe.maintenance.domain.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Profile("jpa")
public class MaintenanceRecordJpaRepository implements MaintenanceRecordRepository {

    private final SpringDataMaintenanceRecordRepository springRepo;
    private final SpringDataMaintenancePartRepository partSpringRepo;
    private final SpringDataMaintenanceTemplateRepository templateSpringRepo;
    private final Replicator replicator;
    private final EventPublisher events;

    public MaintenanceRecordJpaRepository(SpringDataMaintenanceRecordRepository springRepo,
                                          SpringDataMaintenancePartRepository partSpringRepo,
                                          SpringDataMaintenanceTemplateRepository templateSpringRepo,
                                          Replicator replicator,
                                          EventPublisher events) {
        this.springRepo = springRepo;
        this.partSpringRepo = partSpringRepo;
        this.templateSpringRepo = templateSpringRepo;
        this.replicator = replicator;
        this.events = events;
    }

    @Override
    public long count() {
        return springRepo.count();
    }

    @Override
    public List<MaintenanceRecord> findAll() {
        return springRepo.findAll().stream()
                .map(MaintenanceRecordMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<MaintenanceRecord> findByRecordId(UUID recordId) {
        return springRepo.findByRecordId(recordId).map(MaintenanceRecordMapper::toDomain);
    }

    @Override
    public Long findVersionFor(UUID recordId) {
        return springRepo.findVersionFor(recordId);
    }

    @Override
    public MaintenanceRecord save(MaintenanceRecord record) {
        List<MaintenancePartJpaEntity> partsJpa = record.getParts().stream()
                .map(p -> partSpringRepo.findByPartNumber(p.getPartNumber())
                        .orElseThrow(() -> new MaintenancePartNotFoundException("Part not found: " + p.getPartNumber())))
                .collect(Collectors.toList());
        MaintenanceTemplateJpaEntity templateJpa = templateSpringRepo.findByName(record.getTemplate().getName())
                .orElseThrow(() -> new MaintenanceTemplateNotFoundException("Template not found: " + record.getTemplate().getName()));

        MaintenanceRecordJpaEntity existing = springRepo.findByRecordId(record.getRecordId()).orElse(null);

        MaintenanceRecordJpaEntity saved;
        if (existing != null) {
            existing.setStatus(record.getStatus());
            existing.setNotes(record.getNotes());
            existing.setCompletedAt(record.getCompletedAt());
            saved = springRepo.saveAndFlush(existing);
        } else {
            MaintenanceRecordJpaEntity jpaEntity = new MaintenanceRecordJpaEntity(
                    record.getRecordId(), record.getDescription(), record.getStartDate(), record.getExpectedDuration(),
                    record.getNotes(), partsJpa, templateJpa, record.getStatus(),
                    new HashSet<>(record.getComponents()), new RegistrationNumberJpaEmbeddable(record.getAircraftRegistration().getNumber()),
                    record.getCost());
            jpaEntity.setCompletedAt(record.getCompletedAt());
            saved = springRepo.saveAndFlush(jpaEntity);
        }
        // Record: copied to the other holder (home replica and its successor)
        replicator.recordSaved(MaintenanceRecordReplicaHandler.TYPE, record.getRecordId().toString(),
                MaintenanceRecordReplica.from(record), saved.getVersion());
        events.changed(MaintenanceRecordReplicaHandler.TYPE, MaintenanceRecordProjection.ENTITY,
                existing != null ? "updated" : "created", record.getRecordId().toString(), record.getStatus(),
                saved.getVersion(), MaintenanceRecordReplica.from(record));
        return record;
    }

    @Override
    public void delete(MaintenanceRecord record) {
        springRepo.findByRecordId(record.getRecordId()).ifPresent(entity -> {
            springRepo.delete(entity);
            replicator.recordDeleted(MaintenanceRecordReplicaHandler.TYPE, record.getRecordId().toString());
            events.changed(MaintenanceRecordReplicaHandler.TYPE, MaintenanceRecordProjection.ENTITY, "deleted",
                    record.getRecordId().toString(), null, null, null);
        });
    }

}
