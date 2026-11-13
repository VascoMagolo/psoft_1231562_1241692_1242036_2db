package aisafe.flights.infrastructure.readmodel;

import aisafe.flights.infrastructure.replication.FlightReplica;
import aisafe.flights.infrastructure.replication.FlightReplicaHandler;
import aisafe.shared.infrastructure.readmodel.Projection;
import aisafe.shared.infrastructure.replication.ReplicaHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Scheduled flights in the read model, as replicated ({@link FlightReplica}): listings, overlap checks and utilization reports. */
@Component
public class FlightProjection implements Projection {

    public static final String ENTITY = "flight";

    private final FlightReplicaHandler handler;
    private final ObjectMapper objectMapper;

    public FlightProjection(FlightReplicaHandler handler, ObjectMapper objectMapper) {
        this.handler = handler;
        this.objectMapper = objectMapper;
    }

    @Override public String entity() { return ENTITY; }
    @Override public ReplicaHandler<?> handler() { return handler; }

    @Override
    public Object view(JsonNode replica) {
        return objectMapper.treeToValue(replica, FlightReplica.class);
    }
}
