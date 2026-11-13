package aisafe.flights.infrastructure.readmodel;

import aisafe.flights.application.FlightsAcrossReplicas;
import aisafe.flights.domain.ScheduledFlight;
import aisafe.flights.infrastructure.replication.FlightReplica;
import aisafe.shared.application.Versioned;
import aisafe.shared.infrastructure.readmodel.ReadModelStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * {@link FlightsAcrossReplicas} read from this replica's read model, when events are on: the
 * listings and reports no longer ask the peers, they read the local read model, which already has
 * the flights of every replica. Without events, {@code GatheredFlights} asks the peers instead.
 */
@Component
@ConditionalOnProperty(name = "aisafe.events.enabled", havingValue = "true")
public class ReadModelFlights implements FlightsAcrossReplicas {

    private final ReadModelStore store;
    private final ObjectMapper objectMapper;

    public ReadModelFlights(ReadModelStore store, ObjectMapper objectMapper) {
        this.store = store;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Versioned<ScheduledFlight>> findAll() {
        return store.findAll(FlightProjection.ENTITY).stream()
                .map(row -> {
                    ScheduledFlight flight = objectMapper.treeToValue(row.view(), FlightReplica.class).toDomain();
                    return new Versioned<>(flight, row.version() != null ? row.version() : flight.getVersion());
                })
                .toList();
    }
}
