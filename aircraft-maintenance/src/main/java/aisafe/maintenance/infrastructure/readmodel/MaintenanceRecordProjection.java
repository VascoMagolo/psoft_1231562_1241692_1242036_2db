package aisafe.maintenance.infrastructure.readmodel;

import aisafe.maintenance.infrastructure.replication.MaintenanceRecordReplica;
import aisafe.maintenance.infrastructure.replication.MaintenanceRecordReplicaHandler;
import aisafe.shared.infrastructure.readmodel.Projection;
import aisafe.shared.infrastructure.replication.ReplicaHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Maintenance records in the read model, as replicated ({@link MaintenanceRecordReplica}). */
@Component
public class MaintenanceRecordProjection implements Projection {

    public static final String ENTITY = "maintenancerecord";

    private final MaintenanceRecordReplicaHandler handler;
    private final ObjectMapper objectMapper;

    public MaintenanceRecordProjection(MaintenanceRecordReplicaHandler handler, ObjectMapper objectMapper) {
        this.handler = handler;
        this.objectMapper = objectMapper;
    }

    @Override public String entity() { return ENTITY; }
    @Override public ReplicaHandler<?> handler() { return handler; }

    @Override
    public Object view(JsonNode replica) {
        return objectMapper.treeToValue(replica, MaintenanceRecordReplica.class);
    }
}
