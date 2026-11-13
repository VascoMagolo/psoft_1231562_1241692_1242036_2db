package aisafe.flights.infrastructure.replication;

import aisafe.flights.application.FlightsAcrossReplicas;
import aisafe.flights.domain.ScheduledFlight;
import aisafe.shared.application.Versioned;
import aisafe.shared.infrastructure.replication.ReplicaGatherer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {@link FlightsAcrossReplicas} built from the flights held here and by the peers. When events are
 * on, the read model ({@code ReadModelFlights}) is used instead.
 */
@Component
@ConditionalOnProperty(name = "aisafe.events.enabled", havingValue = "false", matchIfMissing = true)
public class GatheredFlights implements FlightsAcrossReplicas {

    private final ReplicaGatherer gatherer;
    private final FlightReplicaHandler handler;

    public GatheredFlights(ReplicaGatherer gatherer, FlightReplicaHandler handler) {
        this.gatherer = gatherer;
        this.handler = handler;
    }

    @Override
    public List<Versioned<ScheduledFlight>> findAll() {
        return gatherer.gather(handler).stream()
                .map(item -> new Versioned<>(item.payload().toDomain(), item.version()))
                .toList();
    }
}
