package aisafe.flightcopies.infrastructure.readmodel;

import aisafe.shared.infrastructure.readmodel.ReadModelStore;
import aisafe.flightcopies.application.FlightCopiesAcrossReplicas;
import aisafe.flightcopies.domain.FlightCopy;
import aisafe.flightcopies.infrastructure.replication.FlightCopyReplica;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * {@link FlightCopiesAcrossReplicas} read from this replica's read model, when events are on: route
 * popularity (US214) no longer asks the peers. Without events, {@code GatheredFlightCopies} asks
 * the peers instead.
 */
@Component
@ConditionalOnProperty(name = "aisafe.events.enabled", havingValue = "true")
public class ReadModelFlightCopies implements FlightCopiesAcrossReplicas {

    private final ReadModelStore store;
    private final ObjectMapper objectMapper;

    public ReadModelFlightCopies(ReadModelStore store, ObjectMapper objectMapper) {
        this.store = store;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<FlightCopy> findAll() {
        return store.findAll(FlightCopyProjection.ENTITY).stream()
                .map(row -> objectMapper.treeToValue(row.view(), FlightCopyReplica.class).toDomain())
                .toList();
    }
}
