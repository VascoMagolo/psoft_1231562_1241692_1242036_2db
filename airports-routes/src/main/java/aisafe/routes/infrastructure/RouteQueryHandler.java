package aisafe.routes.infrastructure;

import aisafe.routes.application.ViewRouteForServicesUseCase;
import aisafe.routes.application.dtos.RouteForServicesResponse;
import aisafe.shared.application.PeerQueries;
import aisafe.shared.domain.NotFoundException;
import aisafe.shared.infrastructure.messaging.CommandHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;
import tools.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;

/**
 * Contract C2 over RabbitMQ ({@value #TYPE}, {@code {"origin": "LIS", "destination": "OPO"}}), for
 * flight-ops when its copy of the routes does not have one yet. Answered from the write model, as
 * over HTTP: this replica's, or a peer's when another replica holds the route.
 */
@Component
public class RouteQueryHandler implements CommandHandler {

    public static final String TYPE = "route.get";

    private final ViewRouteForServicesUseCase viewRoute;
    private final PeerQueries peers;

    public RouteQueryHandler(ViewRouteForServicesUseCase viewRoute, PeerQueries peers) {
        this.viewRoute = viewRoute;
        this.peers = peers;
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public RouteForServicesResponse handle(JsonNode payload) {
        String origin = payload.get("origin").asString().trim().toUpperCase();
        String destination = payload.get("destination").asString().trim().toUpperCase();
        try {
            return viewRoute.execute(origin, destination);
        } catch (NotFoundException notHere) {
            return peers.fetchFirst("/internal/routes/" + segment(origin) + "/" + segment(destination),
                    RouteForServicesResponse.class).orElseThrow(() -> notHere);
        }
    }

    private static String segment(String value) {
        return UriUtils.encodePathSegment(value, StandardCharsets.UTF_8);
    }
}
