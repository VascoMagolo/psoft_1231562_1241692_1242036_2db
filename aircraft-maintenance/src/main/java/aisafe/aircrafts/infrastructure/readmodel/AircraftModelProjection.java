package aisafe.aircrafts.infrastructure.readmodel;

import aisafe.aircrafts.infrastructure.replication.AircraftModelReplica;
import aisafe.aircrafts.infrastructure.replication.AircraftModelReplicaHandler;
import aisafe.shared.infrastructure.readmodel.Projection;
import aisafe.shared.infrastructure.replication.ReplicaHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Aircraft models in the read model ({@link AircraftModelView}, without the image bytes). They are
 * read by airports-routes and flight-ops instead of asking C3, and give them the fuel capacity the
 * fuel consumption of an aircraft is calculated from.
 */
@Component
public class AircraftModelProjection implements Projection {

    public static final String ENTITY = "aircraftmodel";

    private final AircraftModelReplicaHandler handler;
    private final ObjectMapper objectMapper;

    public AircraftModelProjection(AircraftModelReplicaHandler handler, ObjectMapper objectMapper) {
        this.handler = handler;
        this.objectMapper = objectMapper;
    }

    @Override public String entity() { return ENTITY; }
    @Override public ReplicaHandler<?> handler() { return handler; }

    @Override
    public Object view(JsonNode replica) {
        return AircraftModelView.from(objectMapper.treeToValue(replica, AircraftModelReplica.class));
    }
}
