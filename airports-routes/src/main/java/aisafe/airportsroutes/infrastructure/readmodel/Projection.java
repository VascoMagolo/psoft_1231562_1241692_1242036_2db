package aisafe.airportsroutes.infrastructure.readmodel;

import aisafe.shared.infrastructure.replication.ReplicaHandler;
import tools.jackson.databind.JsonNode;

/**
 * One kind of entity kept by the read model (CQRS): how its events are named, where the
 * replicas hold it, and what the read model keeps of it (its view, ready for the queries).
 */
public interface Projection {

    /** First part of the routing keys of its events ({@code route}, {@code airport}...) and its type in the read model. */
    String entity();

    /** The replication of this entity: what every replica exports, for the reconciliation. */
    ReplicaHandler<?> handler();

    /** The view the read model keeps, from the entity as the replication exports it. */
    Object view(JsonNode replica);
}
