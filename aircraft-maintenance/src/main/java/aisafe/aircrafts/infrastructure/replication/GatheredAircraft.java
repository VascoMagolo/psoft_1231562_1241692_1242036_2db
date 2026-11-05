package aisafe.aircrafts.infrastructure.replication;

import aisafe.aircrafts.application.AircraftAcrossReplicas;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelRepository;
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
 * {@link AircraftAcrossReplicas} built from the aircraft held here and by the peers.
 * When events are on, the read model ({@code ReadModelAircraft}) is used instead.
 */
@Component
@ConditionalOnProperty(name = "aisafe.events.enabled", havingValue = "false", matchIfMissing = true)
public class GatheredAircraft implements AircraftAcrossReplicas {

    private static final Logger log = LoggerFactory.getLogger(GatheredAircraft.class);

    private final ReplicaGatherer gatherer;
    private final AircraftReplicaHandler handler;
    private final AircraftModelRepository modelRepository;

    public GatheredAircraft(ReplicaGatherer gatherer, AircraftReplicaHandler handler, AircraftModelRepository modelRepository) {
        this.gatherer = gatherer;
        this.handler = handler;
        this.modelRepository = modelRepository;
    }

    @Override
    public List<Aircraft> findAll() {
        Map<String, AircraftModel> models = modelRepository.findAll().stream()
                .collect(Collectors.toMap(AircraftModel::getModelName, Function.identity()));
        return gatherer.gather(handler).stream()
                .map(item -> {
                    AircraftModel model = models.get(item.payload().modelName());
                    if (model == null) {
                        // Models are copied to every replica; this one has not received it yet
                        log.warn("Aircraft {} left out: model {} not on this replica yet", item.key(), item.payload().modelName());
                        return null;
                    }
                    return item.payload().toDomain(model);
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
