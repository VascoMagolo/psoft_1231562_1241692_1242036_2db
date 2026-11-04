package aisafe.airports.infrastructure.readmodel;

import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.infrastructure.replication.AirportReplica;
import aisafe.airports.infrastructure.replication.AirportReplicaHandler;
import aisafe.shared.infrastructure.readmodel.Projection;
import aisafe.shared.infrastructure.replication.ReplicaHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Airports in the read model: the answer the clients get ({@link AirportResponse}: the photos only
 * counted, not their bytes), for the airport listings.
 */
@Component
public class AirportProjection implements Projection {

    public static final String ENTITY = "airport";

    private final AirportReplicaHandler handler;
    private final ObjectMapper objectMapper;

    public AirportProjection(AirportReplicaHandler handler, ObjectMapper objectMapper) {
        this.handler = handler;
        this.objectMapper = objectMapper;
    }

    @Override public String entity() { return ENTITY; }
    @Override public ReplicaHandler<?> handler() { return handler; }

    @Override
    public Object view(JsonNode replica) {
        return AirportResponse.from(objectMapper.treeToValue(replica, AirportReplica.class).toDomain());
    }
}
