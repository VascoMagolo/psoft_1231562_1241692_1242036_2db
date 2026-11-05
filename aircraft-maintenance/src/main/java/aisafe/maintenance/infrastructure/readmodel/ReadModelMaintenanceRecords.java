package aisafe.maintenance.infrastructure.readmodel;

import aisafe.maintenance.application.MaintenanceRecordsAcrossReplicas;
import aisafe.maintenance.domain.MaintenancePart;
import aisafe.maintenance.domain.MaintenancePartRepository;
import aisafe.maintenance.domain.MaintenanceRecord;
import aisafe.maintenance.domain.MaintenanceTemplate;
import aisafe.maintenance.domain.MaintenanceTemplateRepository;
import aisafe.maintenance.infrastructure.replication.MaintenanceRecordReplica;
import aisafe.shared.application.Versioned;
import aisafe.shared.infrastructure.readmodel.ReadModelStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@link MaintenanceRecordsAcrossReplicas} read from this replica's read model, when events are on
 * (see {@code ReadModelAircraft}). Without events, {@code GatheredMaintenanceRecords} asks the peers.
 * Parts and templates come from their catalogs (on every replica).
 */
@Component
@ConditionalOnProperty(name = "aisafe.events.enabled", havingValue = "true")
public class ReadModelMaintenanceRecords implements MaintenanceRecordsAcrossReplicas {

    private static final Logger log = LoggerFactory.getLogger(ReadModelMaintenanceRecords.class);

    private final ReadModelStore store;
    private final MaintenancePartRepository partRepository;
    private final MaintenanceTemplateRepository templateRepository;
    private final ObjectMapper objectMapper;

    public ReadModelMaintenanceRecords(ReadModelStore store, MaintenancePartRepository partRepository,
                                       MaintenanceTemplateRepository templateRepository, ObjectMapper objectMapper) {
        this.store = store;
        this.partRepository = partRepository;
        this.templateRepository = templateRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Versioned<MaintenanceRecord>> findAll() {
        Map<String, MaintenancePart> parts = partRepository.findAll().stream()
                .collect(Collectors.toMap(MaintenancePart::getPartNumber, Function.identity()));
        Map<String, MaintenanceTemplate> templates = templateRepository.findAll().stream()
                .collect(Collectors.toMap(MaintenanceTemplate::getName, Function.identity()));
        return store.findAll(MaintenanceRecordProjection.ENTITY).stream()
                .map(row -> {
                    MaintenanceRecordReplica replica = objectMapper.treeToValue(row.view(), MaintenanceRecordReplica.class);
                    MaintenanceTemplate template = templates.get(replica.templateName());
                    List<MaintenancePart> recordParts = replica.partNumbers().stream().map(parts::get).toList();
                    if (template == null || recordParts.contains(null)) {
                        // Templates and parts are copied to every replica; this one has not received them yet
                        log.warn("Maintenance record {} left out: its template or parts are not on this replica yet", row.key());
                        return null;
                    }
                    return new Versioned<>(replica.toDomain(recordParts, template), row.version());
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
