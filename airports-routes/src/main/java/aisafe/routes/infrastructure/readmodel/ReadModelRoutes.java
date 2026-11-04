package aisafe.routes.infrastructure.readmodel;

import aisafe.shared.infrastructure.readmodel.ReadModelStore;
import aisafe.routes.application.RoutesAcrossReplicas;
import aisafe.routes.domain.Route;
import aisafe.shared.application.Versioned;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * {@link RoutesAcrossReplicas} read from this replica's read model, when events are on: the
 * listings no longer ask the peers, they read the local read model, which already has the routes
 * of every replica. Without events, {@code GatheredRoutes} asks the peers instead.
 */
@Component
@ConditionalOnProperty(name = "aisafe.events.enabled", havingValue = "true")
public class ReadModelRoutes implements RoutesAcrossReplicas {

    private final ReadModelStore store;
    private final ObjectMapper objectMapper;

    public ReadModelRoutes(ReadModelStore store, ObjectMapper objectMapper) {
        this.store = store;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Versioned<Route>> findAll() {
        return store.findAll(RouteProjection.ENTITY).stream()
                .map(row -> new Versioned<>(objectMapper.treeToValue(row.view(), RouteView.class).toRoute(), row.version()))
                .toList();
    }
}
