package aisafe.shared.infrastructure.readmodel;

import aisafe.shared.infrastructure.messaging.CommandHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Answers the command {@value #TYPE} ({@code {"entity": "aircraft"}}) with every row this
 * replica's read model has of one of this service's entities, deleted ones included, each with the
 * stamp of its change: what another service needs to fill its copy (see {@link SnapshotClient}).
 * It reads the query side, never the write model, and any replica can answer, since each read model
 * holds the entities of every replica.
 */
@Component
public class SnapshotHandler implements CommandHandler {

    public static final String TYPE = "snapshot";

    private final ReadModelStore store;
    private final Set<String> entities;

    public SnapshotHandler(ReadModelStore store, ObjectProvider<Projection> projections) {
        this.store = store;
        this.entities = projections.orderedStream().map(Projection::entity).collect(Collectors.toSet());
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public List<ReadModelStore.StampedRow> handle(JsonNode payload) {
        JsonNode field = payload.get("entity");
        String entity = field == null || field.isNull() ? null : field.asString();
        if (entity == null || !entities.contains(entity)) {
            throw new IllegalArgumentException("No snapshot of " + entity + " here; there are " + entities);
        }
        return store.findAllStamped(entity);
    }
}
