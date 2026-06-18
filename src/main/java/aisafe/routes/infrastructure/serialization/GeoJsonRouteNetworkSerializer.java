package aisafe.routes.infrastructure.serialization;

import aisafe.airports.domain.Airport;
import aisafe.routes.application.RouteNetworkSerializer;
import aisafe.shared.application.ExportedFile;
import aisafe.routes.domain.Route;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GeoJsonRouteNetworkSerializer implements RouteNetworkSerializer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ExportedFile serialize(List<Route> routes, Map<String, Airport> airports) {
        ObjectNode featureCollection = objectMapper.createObjectNode();
        featureCollection.put("type", "FeatureCollection");
        ArrayNode features = featureCollection.putArray("features");

        for (Route route : routes) {
            Airport origin = airports.get(route.getOrigin().getCode());
            Airport destination = airports.get(route.getDestination().getCode());

            ObjectNode feature = features.addObject();
            feature.put("type", "Feature");

            ObjectNode geometry = feature.putObject("geometry");
            geometry.put("type", "LineString");
            ArrayNode coordinates = geometry.putArray("coordinates");
            
            ArrayNode originCoords = coordinates.addArray();
            originCoords.add(origin.getCoordinates().getLongitude());
            originCoords.add(origin.getCoordinates().getLatitude());

            ArrayNode destCoords = coordinates.addArray();
            destCoords.add(destination.getCoordinates().getLongitude());
            destCoords.add(destination.getCoordinates().getLatitude());

            ObjectNode properties = feature.putObject("properties");
            properties.put("origin", route.getOrigin().getCode());
            properties.put("destination", route.getDestination().getCode());
            properties.put("distanceKm", route.getMinimumRange()); // Example property
        }

        try {
            byte[] content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(featureCollection);
            return new ExportedFile(content, "application/geo+json", "routes.geojson");
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize to GeoJSON", e);
        }
    }

    @Override
    public boolean supports(String format) {
        return "geojson".equalsIgnoreCase(format);
    }
}
