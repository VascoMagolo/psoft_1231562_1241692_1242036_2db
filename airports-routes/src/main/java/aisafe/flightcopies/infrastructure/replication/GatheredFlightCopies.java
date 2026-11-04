package aisafe.flightcopies.infrastructure.replication;

import aisafe.flightcopies.application.FlightCopiesAcrossReplicas;
import aisafe.flightcopies.domain.FlightCopy;
import aisafe.shared.infrastructure.replication.ReplicaGatherer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {@link FlightCopiesAcrossReplicas} built from the copies held here and by the peers. When events
 * are on, the read model ({@code ReadModelFlightCopies}) is used instead.
 */
@Component
@ConditionalOnProperty(name = "aisafe.events.enabled", havingValue = "false", matchIfMissing = true)
public class GatheredFlightCopies implements FlightCopiesAcrossReplicas {

    private final ReplicaGatherer gatherer;
    private final FlightCopyReplicaHandler handler;

    public GatheredFlightCopies(ReplicaGatherer gatherer, FlightCopyReplicaHandler handler) {
        this.gatherer = gatherer;
        this.handler = handler;
    }

    @Override
    public List<FlightCopy> findAll() {
        return gatherer.gather(handler).stream()
                .map(item -> item.payload().toDomain())
                .toList();
    }
}
