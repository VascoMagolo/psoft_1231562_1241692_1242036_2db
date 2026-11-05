package aisafe.maintenance.infrastructure.replication;

import aisafe.maintenance.application.MaintenanceRecordsAcrossReplicas;
import aisafe.maintenance.domain.MaintenancePart;
import aisafe.maintenance.domain.MaintenancePartRepository;
import aisafe.maintenance.domain.MaintenanceRecord;
import aisafe.maintenance.domain.MaintenanceTemplate;
import aisafe.maintenance.domain.MaintenanceTemplateRepository;
import aisafe.shared.application.Versioned;
import aisafe.shared.infrastructure.replication.ReplicaGatherer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@link MaintenanceRecordsAcrossReplicas} built from the records held here and by the peers.
 * When events are on, the read model ({@code ReadModelMaintenanceRecords}) is used instead.
 */
@Component
@ConditionalOnProperty(name = "aisafe.events.enabled", havingValue = "false", matchIfMissing = true)
public class GatheredMaintenanceRecords implements MaintenanceRecordsAcrossReplicas {

    private static final Logger log = LoggerFactory.getLogger(GatheredMaintenanceRecords.class);

    private final ReplicaGatherer gatherer;
    private final MaintenanceRecordReplicaHandler handler;
    private final MaintenancePartRepository partRepository;
    private final MaintenanceTemplateRepository templateRepository;

    public GatheredMaintenanceRecords(ReplicaGatherer gatherer, MaintenanceRecordReplicaHandler handler,
                                      MaintenancePartRepository partRepository,
                                      MaintenanceTemplateRepository templateRepository) {
        this.gatherer = gatherer;
        this.handler = handler;
        this.partRepository = partRepository;
        this.templateRepository = templateRepository;
    }

    @Override
    public List<Versioned<MaintenanceRecord>> findAll() {
        Map<String, MaintenancePart> parts = partRepository.findAll().stream()
                .collect(Collectors.toMap(MaintenancePart::getPartNumber, Function.identity()));
        Map<String, MaintenanceTemplate> templates = templateRepository.findAll().stream()
                .collect(Collectors.toMap(MaintenanceTemplate::getName, Function.identity()));
        return gatherer.gather(handler).stream()
                .map(item -> {
                    MaintenanceRecordReplica replica = item.payload();
                    MaintenanceTemplate template = templates.get(replica.templateName());
                    List<MaintenancePart> recordParts = replica.partNumbers().stream().map(parts::get).toList();
                    if (template == null || recordParts.contains(null)) {
                        // Templates and parts are copied to every replica; this one has not received them yet
                        log.warn("Maintenance record {} left out: its template or parts are not on this replica yet", item.key());
                        return null;
                    }
                    return new Versioned<>(replica.toDomain(recordParts, template), item.version());
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
