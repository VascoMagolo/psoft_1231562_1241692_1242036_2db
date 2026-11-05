package aisafe.routes.infrastructure.readmodel;

import aisafe.shared.infrastructure.readmodel.Projection;
import aisafe.routes.application.RouteDistanceService;
import aisafe.routes.infrastructure.replication.RouteReplica;
import aisafe.routes.infrastructure.replication.RouteReplicaHandler;
import aisafe.shared.infrastructure.replication.ReplicaHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Routes in the read model: a {@link RouteView} each (no history), for the route listings. */
@Component
public class RouteProjection implements Projection {

    public static final String ENTITY = "route";

    private final RouteReplicaHandler handler;
    private final ObjectMapper objectMapper;
    private final RouteDistanceService distances;

    public RouteProjection(RouteReplicaHandler handler, ObjectMapper objectMapper, RouteDistanceService distances) {
        this.handler = handler;
        this.objectMapper = objectMapper;
        this.distances = distances;
    }

    @Override public String entity() { return ENTITY; }
    @Override public ReplicaHandler<?> handler() { return handler; }

    @Override
    public Object view(JsonNode replica) {
        return RouteView.from(objectMapper.treeToValue(replica, RouteReplica.class).toRoute(), distances);
    }
}
