package aisafe.aircrafts.infrastructure.readmodel;

import aisafe.aircrafts.application.AircraftAcrossReplicas;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelRepository;
import aisafe.aircrafts.infrastructure.replication.AircraftReplica;
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
 * {@link AircraftAcrossReplicas} read from this replica's read model, when events are on: the
 * listings and reports of the whole fleet no longer ask the peers, they read the local read model,
 * which already has the aircraft of every replica. Without events, {@code GatheredAircraft} asks
 * the peers instead. The model of each aircraft comes from the models catalog (on every replica).
 */
@Component
@ConditionalOnProperty(name = "aisafe.events.enabled", havingValue = "true")
public class ReadModelAircraft implements AircraftAcrossReplicas {

    private static final Logger log = LoggerFactory.getLogger(ReadModelAircraft.class);

    private final ReadModelStore store;
    private final AircraftModelRepository modelRepository;
    private final ObjectMapper objectMapper;

    public ReadModelAircraft(ReadModelStore store, AircraftModelRepository modelRepository, ObjectMapper objectMapper) {
        this.store = store;
        this.modelRepository = modelRepository;
        this.objectMapper = objectMapper;
    }

    /** Ordered by registration (the key of the read model). */
    @Override
    public List<Aircraft> findAll() {
        Map<String, AircraftModel> models = modelRepository.findAll().stream()
                .collect(Collectors.toMap(AircraftModel::getModelName, Function.identity()));
        return store.findAll(AircraftProjection.ENTITY).stream()
                .map(row -> {
                    AircraftReplica aircraft = objectMapper.treeToValue(row.view(), AircraftReplica.class);
                    AircraftModel model = models.get(aircraft.modelName());
                    if (model == null) {
                        // Models are copied to every replica; this one has not received it yet
                        log.warn("Aircraft {} left out: model {} not on this replica yet", row.key(), aircraft.modelName());
                        return null;
                    }
                    return aircraft.toDomain(model);
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
