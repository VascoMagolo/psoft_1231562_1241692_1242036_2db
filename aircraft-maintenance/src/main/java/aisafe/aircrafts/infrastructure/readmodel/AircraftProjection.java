package aisafe.aircrafts.infrastructure.readmodel;

import aisafe.aircrafts.infrastructure.replication.AircraftReplica;
import aisafe.aircrafts.infrastructure.replication.AircraftReplicaHandler;
import aisafe.shared.infrastructure.readmodel.Projection;
import aisafe.shared.infrastructure.replication.ReplicaHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Aircraft in the read model, as replicated ({@link AircraftReplica}: registration, status,
 * manufacturing date, model name, seats, range, features). It is also the {@code data} of the
 * {@code aircraft.*} events, which airports-routes and flight-ops keep instead of asking C1.
 */
@Component
public class AircraftProjection implements Projection {

    public static final String ENTITY = "aircraft";

    private final AircraftReplicaHandler handler;
    private final ObjectMapper objectMapper;

    public AircraftProjection(AircraftReplicaHandler handler, ObjectMapper objectMapper) {
        this.handler = handler;
        this.objectMapper = objectMapper;
    }

    @Override public String entity() { return ENTITY; }
    @Override public ReplicaHandler<?> handler() { return handler; }

    @Override
    public Object view(JsonNode replica) {
        return objectMapper.treeToValue(replica, AircraftReplica.class);
    }
}
