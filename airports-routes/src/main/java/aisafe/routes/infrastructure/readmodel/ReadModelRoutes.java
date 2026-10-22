package aisafe.routes.infrastructure.readmodel;

import aisafe.routes.application.RoutesAcrossReplicas;
import aisafe.routes.domain.Route;
import aisafe.shared.application.Versioned;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {@link RoutesAcrossReplicas} read from this replica's read model, when events are on: the
 * listings no longer ask the peers, they read the local {@code route_view}, which already has the
 * routes of every replica. Without events, {@code GatheredRoutes} asks the peers instead.
 */
@Component
@ConditionalOnProperty(name = "aisafe.events.enabled", havingValue = "true")
public class ReadModelRoutes implements RoutesAcrossReplicas {

    private final RouteViewStore store;

    public ReadModelRoutes(RouteViewStore store) {
        this.store = store;
    }

    @Override
    public List<Versioned<Route>> findAll() {
        return store.findAll();
    }
}
