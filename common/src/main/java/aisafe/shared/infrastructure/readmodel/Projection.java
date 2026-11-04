package aisafe.shared.infrastructure.readmodel;

import aisafe.shared.infrastructure.replication.ReplicaHandler;
import tools.jackson.databind.JsonNode;

/**
 * One kind of entity of this service kept by its read model (CQRS): how its events are named,
 * where the replicas hold it, and what the read model keeps of it (its view, ready for the
 * queries). Other services can read it too: they ask for it with an {@link ExternalProjection}.
 */
public interface Projection {

    /** First part of the routing keys of its events ({@code route}, {@code airport}...) and its type in the read model. */
    String entity();

    /** The replication of this entity: what every replica exports, for the reconciliation. */
    ReplicaHandler<?> handler();

    /** The view the read model keeps, from the entity as the replication exports it. */
    Object view(JsonNode replica);
}
