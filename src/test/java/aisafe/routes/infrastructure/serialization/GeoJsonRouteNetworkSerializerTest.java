package aisafe.routes.infrastructure.serialization;

import aisafe.airports.domain.Airport;
import aisafe.airports.domain.Coordinates;
import aisafe.airports.domain.IataCode;
import aisafe.routes.application.dtos.ExportedFile;
import aisafe.routes.domain.Route;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GeoJsonRouteNetworkSerializerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GeoJsonRouteNetworkSerializer serializer = new GeoJsonRouteNetworkSerializer(objectMapper);

    @Test
    void ensureGeoJsonSerializationCorrectness() throws Exception {
        // Arrange
        Route route = new Route("OPO", "LIS", 60, 300.0, 100);
        Airport origin = mock(Airport.class);
        when(origin.getIataCode()).thenReturn(new IataCode("OPO"));
        when(origin.getCoordinates()).thenReturn(new Coordinates(41.2481, -8.6814));
        
        Airport destination = mock(Airport.class);
        when(destination.getIataCode()).thenReturn(new IataCode("LIS"));
        when(destination.getCoordinates()).thenReturn(new Coordinates(38.7742, -9.1342));
        
        Map<String, Airport> airports = Map.of("OPO", origin, "LIS", destination);

        // Act
        ExportedFile result = serializer.serialize(List.of(route), airports);

        // Assert
        assertEquals("application/geo+json", result.contentType());
        assertEquals("routes.geojson", result.fileName());
        
        JsonNode node = objectMapper.readTree(result.content());
        assertEquals("FeatureCollection", node.get("type").asText());
        JsonNode feature = node.get("features").get(0);
        assertEquals("Feature", feature.get("type").asText());
        assertEquals("LineString", feature.get("geometry").get("type").asText());
        
        JsonNode coords = feature.get("geometry").get("coordinates");
        assertEquals(-8.6814, coords.get(0).get(0).asDouble());
        assertEquals(41.2481, coords.get(0).get(1).asDouble());
        assertEquals(-9.1342, coords.get(1).get(0).asDouble());
        assertEquals(38.7742, coords.get(1).get(1).asDouble());
    }
}
