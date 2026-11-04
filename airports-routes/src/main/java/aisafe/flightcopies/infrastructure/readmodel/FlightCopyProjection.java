package aisafe.flightcopies.infrastructure.readmodel;

import aisafe.shared.infrastructure.readmodel.Projection;
import aisafe.flightcopies.infrastructure.replication.FlightCopyReplica;
import aisafe.flightcopies.infrastructure.replication.FlightCopyReplicaHandler;
import aisafe.shared.infrastructure.replication.ReplicaHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Flight copies in the read model, as replicated ({@link FlightCopyReplica}): route popularity (US214). */
@Component
public class FlightCopyProjection implements Projection {

    public static final String ENTITY = "flightcopy";

    private final FlightCopyReplicaHandler handler;
    private final ObjectMapper objectMapper;

    public FlightCopyProjection(FlightCopyReplicaHandler handler, ObjectMapper objectMapper) {
        this.handler = handler;
        this.objectMapper = objectMapper;
    }

    @Override public String entity() { return ENTITY; }
    @Override public ReplicaHandler<?> handler() { return handler; }

    @Override
    public Object view(JsonNode replica) {
        return objectMapper.treeToValue(replica, FlightCopyReplica.class);
    }
}
